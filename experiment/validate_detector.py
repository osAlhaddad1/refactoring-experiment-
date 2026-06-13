"""Validates the architecture gate against the hand-made gold set.

For every fixture under gold/ we build a tiny project that contains only the
scaffold + the gate + that fixture's Java files, run the gate, and read how many
violations it reported. A fixture under gold/violating/ should be flagged
(violationCount > 0); a fixture under gold/clean/ should not.

From those results we print recall, precision, F1 and the false-positive /
false-negative counts. This shows the detector is trustworthy before we rely on
its counts in the real experiment.

Run it from this folder:
    python validate_detector.py
(set MVN_CMD and JAVA_HOME first, see README).
"""

import os
import shutil
import tempfile

from gate_runner import load_dotenv, run_gate

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
GOLD = os.path.join(REPO, "gold")

# Files copied into every temp project: the scaffold and the gate, but NOT the
# god-file or the behaviour test (those are baseline-specific).
TEMPLATE_FILES = [
    "pom.xml",
    "src/main/java/com/example/shop/ShopApplication.java",
    "src/main/resources/application.properties",
    "src/test/java/com/example/shop/arch/ArchRules.java",
    "src/test/java/com/example/shop/arch/ArchitectureGateTest.java",
]


def make_project(fixture_dir):
    """Builds a temp project = scaffold + gate + the fixture's Java files."""
    temp = tempfile.mkdtemp(prefix="gold-")

    # copy the scaffold + gate
    for relative_path in TEMPLATE_FILES:
        source = os.path.join(REPO, relative_path.replace("/", os.sep))
        target = os.path.join(temp, relative_path.replace("/", os.sep))
        os.makedirs(os.path.dirname(target), exist_ok=True)
        shutil.copy(source, target)

    # copy the fixture's .java files into src/main/java, keeping their packages
    main_java = os.path.join(temp, "src", "main", "java")
    for root, _dirs, files in os.walk(fixture_dir):
        for name in files:
            if name.endswith(".java"):
                source = os.path.join(root, name)
                relative = os.path.relpath(source, fixture_dir)
                target = os.path.join(main_java, relative)
                os.makedirs(os.path.dirname(target), exist_ok=True)
                shutil.copy(source, target)

    return temp


def list_fixtures(label):
    """Returns (name, path) for each fixture folder under gold/<label>/."""
    base = os.path.join(GOLD, label)
    result = []
    for name in sorted(os.listdir(base)):
        path = os.path.join(base, name)
        if os.path.isdir(path):
            result.append((name, path))
    return result


def main():
    load_dotenv()   # read .env at the repo root (if present), e.g. MVN_CMD / JAVA_HOME
    # build the list of (name, path, should_be_flagged)
    cases = []
    for name, path in list_fixtures("violating"):
        cases.append((name, path, True))
    for name, path in list_fixtures("clean"):
        cases.append((name, path, False))

    true_positives = 0
    true_negatives = 0
    false_positives = 0
    false_negatives = 0

    for name, path, should_flag in cases:
        project = make_project(path)
        report = run_gate(project)
        shutil.rmtree(project, ignore_errors=True)

        if report is None:
            print("ERROR  %s: no report (compile error?)" % name)
            continue

        flagged = report["violationCount"] > 0
        if should_flag and flagged:
            true_positives += 1
        elif should_flag and not flagged:
            false_negatives += 1
        elif not should_flag and flagged:
            false_positives += 1
        else:
            true_negatives += 1

        mark = "ok " if flagged == should_flag else "BAD"
        print("%s  %-32s violations=%d  expected_flag=%s"
              % (mark, name, report["violationCount"], should_flag))

    # recall / precision / f1 (positive class = "violating")
    if true_positives + false_negatives > 0:
        recall = true_positives / (true_positives + false_negatives)
    else:
        recall = 0.0
    if true_positives + false_positives > 0:
        precision = true_positives / (true_positives + false_positives)
    else:
        precision = 0.0
    if precision + recall > 0:
        f1 = 2 * precision * recall / (precision + recall)
    else:
        f1 = 0.0

    print()
    print("true positives : %d" % true_positives)
    print("true negatives : %d" % true_negatives)
    print("false positives: %d" % false_positives)
    print("false negatives: %d" % false_negatives)
    print("recall   : %.3f" % recall)
    print("precision: %.3f" % precision)
    print("f1       : %.3f" % f1)


if __name__ == "__main__":
    main()
