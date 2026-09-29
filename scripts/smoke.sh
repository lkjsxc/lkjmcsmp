#!/usr/bin/env bash
set -euo pipefail

python3 -B -m unittest discover -s scripts -p 'test_smoke_response.py'
python3 -m pip install --no-cache-dir mctools==1.3.0 >/tmp/pip-smoke.log
python3 -B scripts/smoke_rcon.py
