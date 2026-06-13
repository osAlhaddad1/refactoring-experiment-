"""Reads results.csv and makes two plain charts:

  1. violations per approach per baseline   (grouped bar chart)
  2. violations vs difficulty                (line chart)

Each (baseline, approach) cell is averaged over its runs. Rows where the build
failed (empty violation_count) are left out of the averages.

Because every baseline runs on its own branch, each run produces a results.csv
for one baseline. To get all baselines on one chart, pass several files:

    python make_charts.py --input results_simple.csv results_complex.csv results_xcomplex.csv

With no arguments it just reads results.csv next to this script.
"""

import argparse
import csv
import os

import matplotlib
matplotlib.use("Agg")  # save PNG files, do not open a window
import matplotlib.pyplot as plt

from prompts import APPROACH_NAMES

HERE = os.path.dirname(os.path.abspath(__file__))

# baselines from easiest to hardest
DIFFICULTY_ORDER = ["simple", "complex", "xcomplex"]


def load_rows(paths):
    rows = []
    for path in paths:
        # utf-8-sig also strips a UTF-8 BOM if some tool added one
        with open(path, newline="", encoding="utf-8-sig") as f:
            for row in csv.DictReader(f):
                rows.append(row)
    return rows


def mean_violations(rows, baseline, approach):
    """Average violation_count for one cell, or NaN if there is no data."""
    counts = []
    for row in rows:
        if row["baseline"] == baseline and row["approach"] == approach:
            if row["violation_count"] != "":
                counts.append(int(row["violation_count"]))
    if not counts:
        return float("nan")
    return sum(counts) / len(counts)


def ordered(values, preferred):
    """Known values first (in preferred order), then any extras sorted."""
    known = [v for v in preferred if v in values]
    extras = sorted(v for v in values if v not in preferred)
    return known + extras


def bar_chart(rows, baselines, approaches, out_path):
    width = 0.8 / len(baselines)
    positions = list(range(len(approaches)))

    plt.figure(figsize=(10, 6))
    for index, baseline in enumerate(baselines):
        heights = [mean_violations(rows, baseline, approach) for approach in approaches]
        offsets = [p + index * width for p in positions]
        plt.bar(offsets, heights, width=width, label=baseline)

    centers = [p + width * (len(baselines) - 1) / 2 for p in positions]
    plt.xticks(centers, approaches, rotation=30, ha="right")
    plt.ylabel("mean violations")
    plt.title("Violations per approach per baseline")
    plt.legend(title="baseline")
    plt.tight_layout()
    plt.savefig(out_path)
    plt.close()


def line_chart(rows, baselines, approaches, out_path):
    plt.figure(figsize=(8, 6))
    for approach in approaches:
        ys = [mean_violations(rows, baseline, approach) for baseline in baselines]
        plt.plot(baselines, ys, marker="o", label=approach)

    plt.xlabel("baseline (increasing difficulty)")
    plt.ylabel("mean violations")
    plt.title("Violations vs difficulty")
    plt.legend(title="approach")
    plt.tight_layout()
    plt.savefig(out_path)
    plt.close()


def main():
    parser = argparse.ArgumentParser(description="Make charts from results.csv")
    parser.add_argument("--input", nargs="+", default=[os.path.join(HERE, "results.csv")],
                        help="one or more results CSV files")
    parser.add_argument("--outdir", default=HERE, help="where to save the PNG files")
    args = parser.parse_args()

    rows = load_rows(args.input)
    baselines = ordered(set(r["baseline"] for r in rows), DIFFICULTY_ORDER)
    approaches = ordered(set(r["approach"] for r in rows), APPROACH_NAMES)

    bar_path = os.path.join(args.outdir, "chart_violations_by_approach.png")
    line_path = os.path.join(args.outdir, "chart_violations_vs_difficulty.png")
    bar_chart(rows, baselines, approaches, bar_path)
    line_chart(rows, baselines, approaches, line_path)

    print("wrote " + bar_path)
    print("wrote " + line_path)


if __name__ == "__main__":
    main()
