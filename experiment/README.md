# Experiment harness (Python)

Plain Python 3 (standard library only so far). The Maven command is read from
the `MVN_CMD` environment variable so no machine path is hard-coded.

## Detector validation (Phase 3)

Runs the architecture gate on the 16 gold-set fixtures (`../gold/`) and prints
recall / precision / F1, so we know the gate reliably tells violating code from
clean code.

```powershell
$env:JAVA_HOME = "C:\Users\osami\.jdks\corretto-23.0.2"
$env:MVN_CMD   = "C:\Users\osami\AppData\Local\thesis-build-tools\apache-maven-3.9.9\bin\mvn.cmd"
cd experiment
python validate_detector.py
```

Each fixture is built in its own throwaway project that contains only the
scaffold + the gate + that fixture's Java files (never the god-file or the
behaviour test). A `gold/violating/*` fixture should be flagged; a
`gold/clean/*` fixture should not.

## Files

- `gate_runner.py` — shared helpers: run a Maven test, run the gate, read the
  JSON report. Reused later by the experiment runner.
- `validate_detector.py` — the Phase 3 detector-validation script.
