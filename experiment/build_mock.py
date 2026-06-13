"""Builds experiment/mock_response.json from the .java files under
experiment/mock_refactoring/files/.

The result is a hardcoded fake AI response in the AI output contract format
({files, deleted, notes}). It is used with `run_experiment.py --mock` to test
the whole pipeline without calling Gemini. Re-run this script if you change the
mock refactoring.
"""

import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
FILES_DIR = os.path.join(HERE, "mock_refactoring", "files")
OUT = os.path.join(HERE, "mock_response.json")

# The old god-file the refactoring replaces.
DELETED = ["src/main/java/com/example/shop/ShopController.java"]
NOTES = "Refactored the god-file into presentation/application/domain/infrastructure layers."


def main():
    files = []
    for root, _dirs, names in os.walk(FILES_DIR):
        for name in names:
            full = os.path.join(root, name)
            rel = os.path.relpath(full, FILES_DIR).replace("\\", "/")
            with open(full, encoding="utf-8") as f:
                files.append({"path": rel, "content": f.read()})

    files.sort(key=lambda item: item["path"])
    response = {"files": files, "deleted": DELETED, "notes": NOTES}

    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(response, f, indent=2)
    print("wrote %s with %d files" % (OUT, len(files)))


if __name__ == "__main__":
    main()
