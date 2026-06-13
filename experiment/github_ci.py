"""Helpers for the GitHub side of the experiment: worktrees, branches, pull
requests, and reading the GitHub Actions (CI) results.

Everything goes through the `git` and `gh` command-line tools (gh must be
authenticated). The runner uses these so each experiment run becomes a real
branch + pull request whose architecture gate runs in CI.
"""

import json
import os
import shutil
import subprocess
import tempfile
import time

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)

# how often to poll GitHub Actions, and when to give up on a single run
CI_POLL_SECONDS = 10
CI_TIMEOUT_SECONDS = 900


def _run(cmd, cwd, check=True):
    """Runs a command, capturing output. Raises on failure when check is True."""
    result = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True)
    if check and result.returncode != 0:
        raise RuntimeError("command failed: %s\n%s\n%s"
                           % (" ".join(cmd), result.stdout, result.stderr))
    return result


def current_branch():
    return _run(["git", "rev-parse", "--abbrev-ref", "HEAD"], REPO).stdout.strip()


def add_worktree(branch, base):
    """Creates branch `branch` off `base` in a fresh temp worktree (so the main
    working tree is never touched). Returns the worktree path."""
    path = tempfile.mkdtemp(prefix="run-")
    os.rmdir(path)  # git worktree add wants a path that does not exist yet
    _run(["git", "worktree", "add", "-b", branch, path, base], REPO)
    return path


def remove_worktree(path):
    _run(["git", "worktree", "remove", "--force", path], REPO, check=False)
    _run(["git", "worktree", "prune"], REPO, check=False)


def delete_local_branch(branch):
    """Deletes a local branch -- used to clean up a run branch that was created
    but never pushed (for example when the AI call failed)."""
    _run(["git", "branch", "-D", branch], REPO, check=False)


def reset_worktree_to(worktree, base):
    """Restores the worktree to exactly `base`'s content, so each loop iteration
    starts again from the original baseline (not the previous attempt)."""
    _run(["git", "rm", "-rfq", "--", "."], worktree, check=False)
    _run(["git", "clean", "-fdq"], worktree, check=False)
    _run(["git", "checkout", base, "--", "."], worktree)


def commit_and_push(worktree, branch, message):
    """Commits everything in the worktree and pushes the branch. Returns the SHA."""
    _run(["git", "add", "-A"], worktree)
    _run(["git", "commit", "-m", message], worktree)
    _run(["git", "push", "-u", "origin", branch], worktree)
    return _run(["git", "rev-parse", "HEAD"], worktree).stdout.strip()


def create_pr(base, head, title, body):
    """Opens a pull request from head into base. Returns the PR url (or '')."""
    result = _run(["gh", "pr", "create", "--base", base, "--head", head,
                   "--title", title, "--body", body], REPO, check=False)
    return (result.stdout or "").strip()


def run_ids(branch):
    """The set of GitHub Actions run ids that exist for the branch right now."""
    result = _run(["gh", "run", "list", "--branch", branch, "--limit", "50",
                   "--json", "databaseId", "-q", ".[].databaseId"], REPO, check=False)
    return set(line.strip() for line in (result.stdout or "").splitlines() if line.strip())


def wait_for_new_run(branch, already_seen):
    """Waits for a NEW Actions run (one not in already_seen) and returns its id."""
    waited = 0
    while waited < CI_TIMEOUT_SECONDS:
        for run_id in run_ids(branch):
            if run_id not in already_seen:
                return run_id
        time.sleep(CI_POLL_SECONDS)
        waited += CI_POLL_SECONDS
    raise RuntimeError("timed out waiting for a CI run on " + branch)


def wait_until_complete(run_id):
    """Polls until the Actions run finishes; returns its conclusion."""
    waited = 0
    while waited < CI_TIMEOUT_SECONDS:
        result = _run(["gh", "run", "view", str(run_id), "--json", "status,conclusion"], REPO, check=False)
        data = {}
        try:
            data = json.loads(result.stdout)
        except ValueError:
            pass
        if data.get("status") == "completed":
            return data.get("conclusion", "")
        time.sleep(CI_POLL_SECONDS)
        waited += CI_POLL_SECONDS
    raise RuntimeError("timed out waiting for CI run %s to finish" % run_id)


def read_ci_results(run_id):
    """Downloads the run's artifacts and returns what the checks found:
    build_passed, behaviour_passed, gate_passed, violation_count,
    violation_types, and the full report dict (for feeding back in a loop)."""
    out = tempfile.mkdtemp(prefix="ci-")
    _run(["gh", "run", "download", str(run_id), "--dir", out], REPO, check=False)

    behaviour_passed = False
    gate_passed = False
    summary = os.path.join(out, "result-summary", "result.txt")
    if os.path.exists(summary):
        with open(summary, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line == "behaviour=success":
                    behaviour_passed = True
                elif line == "gate=success":
                    gate_passed = True

    # arch-report.json only exists if the code compiled and the gate ran
    build_passed = False
    violation_count = None
    violation_types = []
    report = None
    report_path = os.path.join(out, "arch-report", "arch-report.json")
    if os.path.exists(report_path):
        build_passed = True
        with open(report_path, encoding="utf-8") as f:
            report = json.load(f)
        violation_count = report["violationCount"]
        violation_types = sorted(set(v["type"] for v in report["violations"]))

    shutil.rmtree(out, ignore_errors=True)
    return {
        "build_passed": build_passed,
        "behaviour_passed": behaviour_passed,
        "gate_passed": gate_passed,
        "violation_count": violation_count,
        "violation_types": violation_types,
        "report": report,
    }
