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

# Full path to mvn (or just "mvn" if it is on PATH). Set MVN_CMD to override.
MVN = os.environ.get("MVN_CMD", "mvn")

ARCH_REPORT = os.path.join("target", "arch-report.json")


def run_maven_test(project_dir, test_class):
    """Runs `mvn -Dtest=<test_class> test` in project_dir.

    Returns the finished subprocess result (so the caller can look at the exit
    code and the captured output). We use shell=True because mvn is a .cmd file
    on Windows.
    """
    command = '"%s" -q -Dtest=%s test' % (MVN, test_class)
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
