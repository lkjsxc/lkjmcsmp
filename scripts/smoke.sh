#!/usr/bin/env bash
set -euo pipefail

python3 -m pip install --no-cache-dir mctools==1.3.0 >/tmp/pip-smoke.log
python3 scripts/smoke_rcon.py
