#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1] / "docs"


def list_children(directory: Path):
    return [p for p in directory.iterdir() if not p.name.startswith(".")]


def main() -> int:
    if not ROOT.exists():
        print("docs/ missing")
        return 1

    failures: list[str] = []
    for directory in [ROOT, *[p for p in ROOT.rglob("*") if p.is_dir()]]:
        readme = directory / "README.md"
        if not readme.exists():
            failures.append(f"missing README.md: {directory}")
            continue
        children = [c for c in list_children(directory) if c.name != "README.md"]
        if len(children) < 2:
            failures.append(f"needs >=2 children: {directory}")

    if failures:
        for line in failures:
            print(line)
        return 1

    print("docs topology ok")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
