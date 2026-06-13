"""Small helpers for running the Maven build (behaviour tests and the
architecture gate) inside a project folder and reading the gate's JSON report.

These are shared by the detector-validation script and (later) the experiment
runner, so neither has to repeat the Maven/JSON plumbing.

The Maven command is read from the MVN_CMD environment variable (default "mvn"),
so no machine-specific path is hard-coded. JAVA_HOME is taken from the current
environment as usual.
"""

import json
import os
import subprocess

ARCH_REPORT = os.path.join("target", "arch-report.json")


def load_dotenv():
    """Loads KEY=value lines from a .env file at the repo root into the
    environment, without overriding variables already set in the shell. Blank
    lines and lines starting with # are ignored. Standard library only.

    This is how the GEMINI_API_KEY / GEMINI_MODEL (and optionally MVN_CMD /
    JAVA_HOME) reach the runner without being hard-coded anywhere tracked.
    """
    repo = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    env_path = os.path.join(repo, ".env")
    if not os.path.exists(env_path):
        return
    with open(env_path, encoding="utf-8") as env_file:
        for line in env_file:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip('"').strip("'")
            if key and key not in os.environ:
                os.environ[key] = value


def run_maven_test(project_dir, test_class):
    """Runs `mvn -Dtest=<test_class> test` in project_dir.

    Returns the finished subprocess result (so the caller can look at the exit
    code and the captured output). We use shell=True because mvn is a .cmd file
    on Windows.
    """
    mvn = os.environ.get("MVN_CMD", "mvn")
    command = '"%s" -q -Dtest=%s test' % (mvn, test_class)
    return subprocess.run(
        command,
        cwd=project_dir,
        shell=True,
        capture_output=True,
        text=True,
    )


def run_gate(project_dir):
    """Runs the architecture gate in project_dir and returns the parsed report
    (a dict with "violationCount" and "violations").

    Returns None if no report was written, which usually means the code did not
    compile. The Maven output is printed in that case to help debugging.
    """
    report_path = os.path.join(project_dir, ARCH_REPORT)
    # remove any old report so we never read a stale one
    if os.path.exists(report_path):
        os.remove(report_path)

    result = run_maven_test(project_dir, "ArchitectureGateTest")

    if not os.path.exists(report_path):
        print("---- no arch-report.json was written; maven output (tail) ----")
        print((result.stdout or "")[-1500:])
        return None

    with open(report_path, encoding="utf-8") as report_file:
        return json.load(report_file)
