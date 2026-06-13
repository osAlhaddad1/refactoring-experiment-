"""The experiment runner.

For each (approach x run) it:
  1. copies the project into a fresh temp folder (so runs stay isolated),
  2. builds the prompt for that approach from the god-file in the copy,
  3. calls the AI (or a mock) at temperature 0,
  4. parses the returned JSON and writes/deletes files in the copy,
  5. runs the HTTP behaviour tests, then the architecture gate,
  6. records one row to results.csv and results.json.
Loop approaches (5 and 6) re-prompt with the violation report until the gate
passes or MAX_ITERS is reached.

The baseline name is taken from the current git branch (read-only) or --baseline.
Because every baseline lives on its own branch, the only thing that changes
between branches is the god-file -- this runner stays the same.

Usage examples (set MVN_CMD and JAVA_HOME first, see README):
    python run_experiment.py --mock
    python run_experiment.py --mock --approaches naive-local
    python run_experiment.py --baseline complex          # real Gemini run
"""

import argparse
import csv
import json
import os
import shutil
import subprocess
import tempfile

from ai_client import call_gemini, call_mock
from gate_runner import load_dotenv, run_gate, run_maven_test
from prompts import APPROACH_NAMES, approach_by_name, build_prompt

# ---- settings (change these) ----------------------------------------------

RUNS_PER_CELL = 1       # how many times to repeat each (baseline x approach); raise to 5-10 later
MAX_ITERS = 3           # max loop iterations for the loop approaches

# ---- fixed paths -----------------------------------------------------------

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)

GOD_FILE = "src/main/java/com/example/shop/ShopController.java"
ARCH_RULES_FILE = "src/test/java/com/example/shop/arch/ArchRules.java"
BEHAVIOUR_FILE = "src/test/java/com/example/shop/ShopBehaviourTest.java"

# Files the AI is never allowed to change or delete (the grading harness).
PROTECTED = {
    BEHAVIOUR_FILE,
    ARCH_RULES_FILE,
    "src/test/java/com/example/shop/arch/ArchitectureGateTest.java",
    "src/main/java/com/example/shop/ShopApplication.java",
    "pom.xml",
}

RESULTS_CSV = os.path.join(HERE, "results.csv")
RESULTS_JSON = os.path.join(HERE, "results.json")


# ---- helpers ---------------------------------------------------------------

def current_git_branch():
    """Returns the current git branch name, or None (read-only git call)."""
    try:
        result = subprocess.run(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            cwd=REPO, capture_output=True, text=True,
        )
        if result.returncode == 0:
            return result.stdout.strip()
    except OSError:
        pass
    return None


def copy_project():
    """Copies pom.xml + src/ into a fresh temp folder and returns its path."""
    temp = tempfile.mkdtemp(prefix="run-")
    shutil.copy(os.path.join(REPO, "pom.xml"), os.path.join(temp, "pom.xml"))
    shutil.copytree(os.path.join(REPO, "src"), os.path.join(temp, "src"))
    return temp


def read_file(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def build_context(work):
    """Reads the pieces the prompt needs from the working copy."""
    return {
        "god_file": read_file(os.path.join(work, GOD_FILE.replace("/", os.sep))),
        "arch_rules_source": read_file(os.path.join(work, ARCH_RULES_FILE.replace("/", os.sep))),
        "behaviour_tests": read_file(os.path.join(work, BEHAVIOUR_FILE.replace("/", os.sep))),
    }


def clean_rel(path):
    """Normalises a path from the AI to a forward-slash project-relative path."""
    path = path.replace("\\", "/")
    if path.startswith("./"):
        path = path[2:]
    return path.lstrip("/")


def safe_target(work, rel):
    """Returns an absolute path inside work for rel, or None if it escapes work."""
    target = os.path.normpath(os.path.join(work, rel.replace("/", os.sep)))
    root = os.path.normpath(work)
    if target == root or target.startswith(root + os.sep):
        return target
    return None


def apply_changes(work, parsed):
    """Writes the AI's files and deletes the ones it asked to delete, skipping
    the protected grading files and any unsafe path."""
    for item in parsed.get("files", []):
        rel = clean_rel(item.get("path", ""))
        if rel in PROTECTED:
            print("    (skipped protected file: %s)" % rel)
            continue
        target = safe_target(work, rel)
        if target is None:
            print("    (skipped unsafe path: %s)" % rel)
            continue
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w", encoding="utf-8") as out:
            out.write(item.get("content", ""))

    for raw in parsed.get("deleted", []):
        rel = clean_rel(raw)
        if rel in PROTECTED:
            print("    (skipped protected delete: %s)" % rel)
            continue
        target = safe_target(work, rel)
        if target is not None and os.path.exists(target):
            os.remove(target)


def parse_ai_json(text):
    """Parses the AI's reply into a dict, tolerating code fences and stray text."""
    text = text.strip()
    if text.startswith("```"):
        lines = text.splitlines()
        if lines and lines[0].startswith("```"):
            lines = lines[1:]
        if lines and lines[-1].strip().startswith("```"):
            lines = lines[:-1]
        text = "\n".join(lines).strip()
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        pass
    # last resort: grab the outermost { ... }
    start = text.find("{")
    end = text.rfind("}")
    if start != -1 and end > start:
        try:
            return json.loads(text[start:end + 1])
        except json.JSONDecodeError:
            return None
    return None


def call_ai(prompt, args):
    if args.mock:
        return call_mock(prompt, args.mock_file)
    return call_gemini(prompt)


# ---- one cell --------------------------------------------------------------

def run_cell(baseline, approach, run_number, args):
    """Runs one (baseline x approach x run), looping if it is a loop approach."""
    max_iters = args.max_iters if approach["loop"] else 1

    report_text = None
    iterations = 0
    build_passed = False
    behaviour_passed = False
    violation_count = None
    violation_types = []
    input_tokens = 0
    output_tokens = 0
    latency_ms = 0

    while iterations < max_iters:
        iterations += 1
        work = copy_project()
        try:
            context = build_context(work)
            prompt = build_prompt(approach, context, report_text)

            ai = call_ai(prompt, args)
            input_tokens += ai["input_tokens"]
            output_tokens += ai["output_tokens"]
            latency_ms += ai["latency_ms"]

            parsed = parse_ai_json(ai["text"])
            if parsed is None:
                print("    iteration %d: could not parse the AI JSON" % iterations)
                build_passed = False
                behaviour_passed = False
                violation_count = None
                break

            apply_changes(work, parsed)

            behaviour = run_maven_test(work, "ShopBehaviourTest")
            behaviour_passed = (behaviour.returncode == 0)

            report = run_gate(work)
            if report is None:
                print("    iteration %d: build failed (no gate report)" % iterations)
                build_passed = False
                violation_count = None
                violation_types = []
                break

            build_passed = True
            violation_count = report["violationCount"]
            violation_types = sorted(set(v["type"] for v in report["violations"]))
            print("    iteration %d: behaviour_passed=%s violations=%d"
                  % (iterations, behaviour_passed, violation_count))

            if violation_count == 0:
                break
            report_text = json.dumps(report)  # feed back into the next loop
        finally:
            shutil.rmtree(work, ignore_errors=True)

    return {
        "baseline": baseline,
        "approach": approach["name"],
        "run": run_number,
        "build_passed": build_passed,
        "behaviour_passed": behaviour_passed,
        "violation_count": violation_count,
        "violation_types": violation_types,
        "iterations": iterations,
        "input_tokens": input_tokens,
        "output_tokens": output_tokens,
        "latency_ms": latency_ms,
    }


# ---- results ---------------------------------------------------------------

def write_results(rows):
    with open(RESULTS_CSV, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow([
            "baseline", "approach", "run", "build_passed", "behaviour_passed",
            "violation_count", "violation_types", "iterations",
            "input_tokens", "output_tokens", "latency_ms",
        ])
        for r in rows:
            writer.writerow([
                r["baseline"], r["approach"], r["run"], r["build_passed"], r["behaviour_passed"],
                "" if r["violation_count"] is None else r["violation_count"],
                ";".join(r["violation_types"]), r["iterations"],
                r["input_tokens"], r["output_tokens"], r["latency_ms"],
            ])

    with open(RESULTS_JSON, "w", encoding="utf-8") as f:
        json.dump(rows, f, indent=2)


# ---- main ------------------------------------------------------------------

def parse_args():
    parser = argparse.ArgumentParser(description="Run the refactoring experiment.")
    parser.add_argument("--baseline", default=None,
                        help="baseline name (default: current git branch)")
    parser.add_argument("--mock", action="store_true",
                        help="use the mock AI response instead of calling Gemini")
    parser.add_argument("--mock-file", default=os.path.join(HERE, "mock_response.json"),
                        help="the hardcoded JSON file to use with --mock")
    parser.add_argument("--approaches", nargs="*", default=APPROACH_NAMES,
                        choices=APPROACH_NAMES, help="which approaches to run")
    parser.add_argument("--runs", type=int, default=RUNS_PER_CELL,
                        help="how many runs per (baseline x approach)")
    parser.add_argument("--max-iters", type=int, default=MAX_ITERS,
                        help="max loop iterations for loop approaches")
    return parser.parse_args()


def main():
    load_dotenv()   # read .env at the repo root (if present) before we need the API key
    args = parse_args()
    baseline = args.baseline or current_git_branch() or "unknown"

    print("baseline = %s" % baseline)
    rows = []
    for name in args.approaches:
        approach = approach_by_name(name)
        for run_number in range(1, args.runs + 1):
            print("== %s / %s / run %d ==" % (baseline, name, run_number))
            rows.append(run_cell(baseline, approach, run_number, args))

    write_results(rows)

    print()
    print("wrote %s and %s" % (RESULTS_CSV, RESULTS_JSON))
    print()
    print("%-24s %-6s %-6s %-10s %s" % ("approach", "build", "behav", "violations", "iters"))
    for r in rows:
        vcount = "-" if r["violation_count"] is None else str(r["violation_count"])
        print("%-24s %-6s %-6s %-10s %d"
              % (r["approach"], r["build_passed"], r["behaviour_passed"], vcount, r["iterations"]))


if __name__ == "__main__":
    main()
