# Experiment harness (Python)

Plain Python 3. Standard library only, except `requests` for the real Gemini
call. Configuration (`GEMINI_API_KEY`, `GEMINI_MODEL`, and optionally `MVN_CMD` /
`JAVA_HOME`) is read from the environment, so no machine path or secret is
hard-coded.

### Configuration: a `.env` file (recommended)

Copy `../.env.example` to `../.env` (at the repo root) and fill it in:

```
GEMINI_API_KEY=your-key-here
GEMINI_MODEL=gemini-2.0-flash
MVN_CMD=C:\Users\osami\AppData\Local\thesis-build-tools\apache-maven-3.9.9\bin\mvn.cmd
JAVA_HOME=C:\Users\osami\.jdks\corretto-23.0.2
```

`.env` is gitignored (never committed) and persists across branch checkouts.
`run_experiment.py` and `validate_detector.py` read it automatically on start.
Values in `.env` take precedence over any shell variables of the same name.

You can also just set them in the shell instead:

```powershell
$env:JAVA_HOME = "C:\Users\osami\.jdks\corretto-23.0.2"
$env:MVN_CMD   = "C:\Users\osami\AppData\Local\thesis-build-tools\apache-maven-3.9.9\bin\mvn.cmd"
```

## Detector validation (Phase 3)

Runs the architecture gate on the 16 gold-set fixtures and prints recall /
precision / F1.

```powershell
python validate_detector.py
```

## The runner (Phase 4)

For each (baseline x approach x run) it copies the project into a throwaway
folder, builds the prompt, calls the AI (or a mock), applies the returned files,
runs the behaviour tests and then the gate, and records a row to `results.csv`
and `results.json`.

The **baseline name** comes from the current git branch (or `--baseline`), so
the runner is identical on every branch -- only the god-file differs.

Test the whole pipeline with the mock AI (no tokens spent):

```powershell
python build_mock.py            # (re)build mock_response.json from mock_refactoring/files
python run_experiment.py --mock
```

Real run (needs the Gemini env vars and `pip install requests`):

```powershell
$env:GEMINI_API_KEY = "..."
$env:GEMINI_MODEL   = "gemini-2.0-flash"
python run_experiment.py --baseline simple
```

Useful flags: `--approaches naive-local recipe-loop` (subset), `--runs 5`
(repeat each cell), `--max-iters 3`, `--mock-file mock_response_noop.json`
(a no-op response that keeps failing the gate, to watch the loop retry).

## The six approaches

| # | Name | Guidance | Context | Loop |
|---|------|----------|---------|------|
| 1 | naive-local | none | god-file only | no |
| 2 | naive-module | none | whole module | no |
| 3 | archaware-interfaces | architecture description | god-file + skeleton + example port | no |
| 4 | ruleaware-module | the literal rules | whole module | no |
| 5 | ruleaware-module-loop | the literal rules | whole module | yes |
| 6 | recipe-loop | rules + recipe | whole module | yes |

"Whole module" = the god-file plus three read-only blocks: the architecture
description, the ArchUnit rule source, and the HTTP behaviour tests.

## Charts (Phase 5)

```powershell
python make_charts.py
```

Reads `results.csv` and writes two PNGs: `chart_violations_by_approach.png`
(grouped bar: violations per approach per baseline) and
`chart_violations_vs_difficulty.png` (line: violations vs difficulty).

Each baseline runs on its own branch, so to put all three on one chart, pass the
per-branch CSVs together:

```powershell
python make_charts.py --input results_simple.csv results_complex.csv results_xcomplex.csv
```

## Files

- `prompts.py` — the six approaches and the prompt text blocks.
- `ai_client.py` — Gemini client + mock client.
- `run_experiment.py` — the runner (settings `RUNS_PER_CELL`, `MAX_ITERS` at the top).
- `gate_runner.py` — shared Maven/JSON helpers.
- `validate_detector.py` — Phase 3 detector validation.
- `build_mock.py` + `mock_refactoring/` — build `mock_response.json` (a clean
  4-layer refactoring of the simple baseline, used by `--mock`).
- `mock_response_noop.json` — a do-nothing response for testing the loop.
- `make_charts.py` — Phase 5 charts from `results.csv`.
