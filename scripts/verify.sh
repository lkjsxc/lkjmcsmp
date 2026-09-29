#!/usr/bin/env bash
set -euo pipefail

echo "=== gradle test/build ==="
gradle --no-daemon clean test shadowJar

echo "=== smoke response tests ==="
python3 -B -m unittest discover -s scripts -p 'test_smoke_response.py'

echo "=== docs topology ==="
python3 scripts/validate_docs_topology.py

echo "=== line limits ==="
python3 scripts/check_lines.py

echo "=== verify complete ==="
