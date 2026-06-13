"""The experiment runner (CI / pull-request version).

For each (baseline x approach x run) it:
  1. branches off the current baseline branch into an isolated worktree,
  2. builds the prompt and asks the AI (or a mock) to refactor the god-file,
  3. writes the AI's files, commits, and pushes the branch,
  4. opens a pull request, which makes GitHub Actions run the behaviour tests
     and the architecture gate,
  5. waits for CI, downloads the result, and records one row.
Loop approaches re-prompt with the violation report and push again (CI re-runs)
until the gate is green or MAX_ITERS is reached. Pull requests are left open as
evidence.

Run it from the baseline branch you want to test (simple / complix / xcomplix):
    python run_experiment.py                 # real Gemini
    python run_experiment.py --mock          # push the mock refactoring instead
    python run_experiment.py --approaches naive-local
"""

import argparse
import csv
import json
import os
import sys
import time

import github_ci as ci
from ai_client import call_mock, call_openrouter
from gate_runner import load_dotenv
from prompts import APPROACH_NAMES, approach_by_name, build_prompt

# ---- settings (change these) ----------------------------------------------

RUNS_PER_CELL = 1               # repeats per (baseline x approach); raise to 5-10 later
MAX_ITERS = 3                   # max loop iterations for loop approaches
SECONDS_BETWEEN_AI_CALLS = 0    # OpenRouter has generous limits; no throttle needed (raise if you hit 429s)

# ---- fixed paths -----------------------------------------------------------

HERE = os.path.dirname(os.path.abspath(__file__))

GOD_FILE = "src/main/java/com/example/shop/ShopController.java"
ARCH_RULES_FILE = "src/test/java/com/example/shop/arch/ArchRules.java"
BEHAVIOUR_FILE = "src/test/java/com/example/shop/ShopBehaviourTest.java"

# Files the AI is never allowed to change or delete (the grading harness + CI).
PROTECTED = {
    BEHAVIOUR_FILE,
    ARCH_RULES_FILE,
    "src/test/java/com/example/shop/arch/ArchitectureGateTest.java",
    "src/main/java/com/example/shop/ShopApplication.java",
    "pom.xml",
    ".github/workflows/arch-gate.yml",
}

RESULTS_CSV = os.path.join(HERE, "results.csv")
RESULTS_JSON = os.path.join(HERE, "results.json")


# ---- file helpers ----------------------------------------------------------

def read_file(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def build_context(worktree):
    """Reads the pieces the prompt needs from the worktree (baseline content)."""
    return {
        "god_file": read_file(os.path.join(worktree, GOD_FILE.replace("/", os.sep))),
        "arch_rules_source": read_file(os.path.join(worktree, ARCH_RULES_FILE.replace("/", os.sep))),
        "behaviour_tests": read_file(os.path.join(worktree, BEHAVIOUR_FILE.replace("/", os.sep))),
    }


def clean_rel(path):
    """Normalises a path from the AI to a forward-slash project-relative path."""
    path = path.replace("\\", "/")
    if path.startswith("./"):
        path = path[2:]
    return path.lstrip("/")


def safe_target(root, rel):
    """Returns an absolute path inside root for rel, or None if it escapes root."""
    target = os.path.normpath(os.path.join(root, rel.replace("/", os.sep)))
    base = os.path.normpath(root)
    if target == base or target.startswith(base + os.sep):
        return target
    return None


def apply_changes(worktree, parsed):
    """Writes the AI's files and deletes the ones it asked to delete, skipping
    the protected grading files and any unsafe path."""
    for item in parsed.get("files", []):
        rel = clean_rel(item.get("path", ""))
        if rel in PROTECTED:
            print("    (skipped protected file: %s)" % rel)
            continue
        target = safe_target(worktree, rel)
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
        target = safe_target(worktree, rel)
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
    start = text.find("{")
    end = text.rfind("}")
    if start != -1 and end > start:
        try:
            return json.loads(text[start:end + 1])
        except json.JSONDecodeError:
            return None
    return None


# ---- AI call with rate limiting -------------------------------------------

_last_ai_call_time = 0.0


def call_ai(prompt, args):
    global _last_ai_call_time
    if args.mock:
        return call_mock(prompt, args.mock_file)
    wait = SECONDS_BETWEEN_AI_CALLS - (time.time() - _last_ai_call_time)
    if wait > 0:
        print("    (waiting %ds for the API rate limit)" % int(wait))
        time.sleep(wait)
    _last_ai_call_time = time.time()
    return call_openrouter(prompt)


# ---- one cell --------------------------------------------------------------

def run_cell(baseline, approach, run_number, args):
    """Runs one (baseline x approach x run) as a branch + PR + CI, looping if
    it is a loop approach."""
    branch = "exp/%s/%s/r%d" % (baseline, approach["name"], run_number)
    worktree = ci.add_worktree(branch, baseline)

    max_iters = args.max_iters if approach["loop"] else 1
    report_text = None
    iterations = 0
    build_passed = False
    behaviour_passed = False
    violation_count = None
    violation_types = []
    input_tokens = output_tokens = latency_ms = 0
    pr_url = ""
    pushed = False

    try:
        while iterations < max_iters:
            iterations += 1
            if iterations > 1:
                ci.reset_worktree_to(worktree, baseline)  # start again from the baseline

            context = build_context(worktree)
            prompt = build_prompt(approach, context, report_text)

            ai = call_ai(prompt, args)
            input_tokens += ai["input_tokens"]
            output_tokens += ai["output_tokens"]
            latency_ms += ai["latency_ms"]

            parsed = parse_ai_json(ai["text"])
            if parsed is None:
                print("    iteration %d: could not parse the AI JSON" % iterations)
                break

            apply_changes(worktree, parsed)

            seen_before = ci.run_ids(branch)
            message = "%s / %s / run %d / iteration %d" % (baseline, approach["name"], run_number, iterations)
            committed = ci.commit_and_push(worktree, branch, message)
            if committed is None:
                print("    iteration %d: the AI produced no change; stopping" % iterations)
                break
            pushed = True
            if iterations == 1:
                pr_url = ci.create_pr(
                    baseline, branch,
                    "exp: %s / %s (run %d)" % (baseline, approach["name"], run_number),
                    "Automated experiment run. The architecture gate runs in CI.")

            run_id = ci.wait_for_new_run(branch, seen_before)
            print("    iteration %d: waiting for CI run %s ..." % (iterations, run_id))
            ci.wait_until_complete(run_id)
            result = ci.read_ci_results(run_id)

            build_passed = result["build_passed"]
            behaviour_passed = result["behaviour_passed"]
            violation_count = result["violation_count"]
            violation_types = result["violation_types"]
            print("    iteration %d: build=%s behaviour=%s violations=%s"
                  % (iterations, build_passed, behaviour_passed, violation_count))

            if build_passed and violation_count == 0:
                break  # gate is green, done
            if result["report"] is None:
                break  # build failed: no violation report to feed back, so looping cannot help
            report_text = json.dumps(result["report"])  # feed the violations back for the next iteration
    finally:
        ci.remove_worktree(worktree)
        if not pushed:
            ci.delete_local_branch(branch)  # nothing was pushed, so drop the empty branch

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
        "pr_url": pr_url,
    }


# ---- results ---------------------------------------------------------------

def write_results(rows):
    with open(RESULTS_CSV, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow([
            "baseline", "approach", "run", "build_passed", "behaviour_passed",
            "violation_count", "violation_types", "iterations",
            "input_tokens", "output_tokens", "latency_ms", "pr_url",
        ])
        for r in rows:
            writer.writerow([
                r["baseline"], r["approach"], r["run"], r["build_passed"], r["behaviour_passed"],
                "" if r["violation_count"] is None else r["violation_count"],
                ";".join(r["violation_types"]), r["iterations"],
                r["input_tokens"], r["output_tokens"], r["latency_ms"], r["pr_url"],
            ])

    with open(RESULTS_JSON, "w", encoding="utf-8") as f:
        json.dump(rows, f, indent=2)


# ---- main ------------------------------------------------------------------

def parse_args():
    parser = argparse.ArgumentParser(description="Run the refactoring experiment via CI/PRs.")
    parser.add_argument("--baseline", default=None,
                        help="baseline name for the results (default: current git branch)")
    parser.add_argument("--mock", action="store_true",
                        help="push the mock refactoring instead of calling Gemini")
    parser.add_argument("--mock-file", default=os.path.join(HERE, "mock_response.json"),
                        help="the hardcoded JSON file to use with --mock")
    parser.add_argument("--approaches", nargs="*", default=APPROACH_NAMES,
                        choices=APPROACH_NAMES, help="which approaches to run")
    parser.add_argument("--runs", type=int, default=RUNS_PER_CELL,
                        help="last run number per (baseline x approach)")
    parser.add_argument("--start-run", type=int, default=1,
                        help="first run number (use to add runs without redoing existing ones)")
    parser.add_argument("--max-iters", type=int, default=MAX_ITERS,
                        help="max loop iterations for loop approaches")
    return parser.parse_args()


def main():
    load_dotenv()
    args = parse_args()

    branch = ci.current_branch()
    baseline = args.baseline or branch
    print("running on branch '%s' (baseline label: '%s')" % (branch, baseline))

    # Append to whatever results already exist, so approaches/baselines run in
    # separate invocations all accumulate into one results file.
    rows = []
    if os.path.exists(RESULTS_JSON):
        with open(RESULTS_JSON, encoding="utf-8") as f:
            rows = json.load(f)

    quota_failures = 0
    for name in args.approaches:
        approach = approach_by_name(name)
        for run_number in range(args.start_run, args.runs + 1):
            print("== %s / %s / run %d ==" % (baseline, name, run_number))
            try:
                rows.append(run_cell(baseline, approach, run_number, args))
                write_results(rows)   # save after every cell so a long run never loses progress
                quota_failures = 0
            except Exception as error:
                message = str(error)
                print("    !! cell failed (%s: %s); skipping. Re-run later with --approaches %s"
                      % (error.__class__.__name__, message, name))
                # If the API quota is exhausted (429), every cell will fail -- stop
                # after two in a row instead of grinding through all of them.
                if "429" in message or "Too Many Requests" in message or "RESOURCE_EXHAUSTED" in message:
                    quota_failures += 1
                    if quota_failures >= 2:
                        print("    !! Two cells in a row hit the API quota (429). Stopping. "
                              "Wait for the quota to reset (or enable billing), then re-run.")
                        write_results(rows)
                        return

    print()
    print("wrote %s and %s" % (RESULTS_CSV, RESULTS_JSON))
    print()
    print("%-22s %-6s %-6s %-10s %-5s %s" % ("approach", "build", "behav", "violations", "iters", "pr"))
    for r in rows:
        vcount = "-" if r["violation_count"] is None else str(r["violation_count"])
        print("%-22s %-6s %-6s %-10s %-5d %s"
              % (r["approach"], r["build_passed"], r["behaviour_passed"], vcount, r["iterations"], r["pr_url"]))


if __name__ == "__main__":
    main()
