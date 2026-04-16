#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
DOC_LIMIT = 300
SRC_LIMIT = 200


def count_lines(path: Path) -> int:
    return path.read_text(encoding="utf-8").count("\n") + 1


def iter_files(base: Path, suffixes: tuple[str, ...]):
    for path in base.rglob("*"):
        if path.is_file() and path.suffix in suffixes:
            yield path


def main() -> int:
    failures: list[str] = []
    for path in iter_files(ROOT / "docs", (".md",)):
        lines = count_lines(path)
        if lines > DOC_LIMIT:
            failures.append(f"docs limit exceeded: {path} ({lines}>{DOC_LIMIT})")

    src_roots = [ROOT / "src" / "main" / "java", ROOT / "src" / "test" / "java"]
    for root in src_roots:
        if not root.exists():
            continue
        for path in iter_files(root, (".java",)):
            lines = count_lines(path)
            if lines > SRC_LIMIT:
                failures.append(f"source limit exceeded: {path} ({lines}>{SRC_LIMIT})")

    if failures:
        for line in failures:
            print(line)
        return 1

    print("line limits ok")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
