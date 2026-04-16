#!/usr/bin/env python3
import os
import sys
import time
from pathlib import Path
from mctools import RCONClient


HOST = os.environ.get("RCON_HOST", "folia")
PORT = int(os.environ.get("RCON_PORT", "25575"))
PASSWORD = os.environ.get("RCON_PASSWORD", "lkjmcsmp-rcon")
WORKSPACE = Path("/workspace")
SOURCE_CONFIG = WORKSPACE / "src" / "main" / "resources" / "config.yml"


def run_cmd(client: RCONClient, command: str, must_contain: str | None = None):
    result = client.command(command)
    if must_contain and must_contain.lower() not in result.lower():
        raise RuntimeError(f"command `{command}` missing `{must_contain}` in `{result}`")
    print(f"[ok] {command}: {result[:160]}")


def connect_with_retry():
    for _ in range(30):
        try:
            client = RCONClient(HOST, port=PORT)
            if client.login(PASSWORD):
                return client
        except Exception:
            pass
        time.sleep(2)
    raise RuntimeError("unable to connect to RCON")


def assert_radius_defaults():
    if not SOURCE_CONFIG.exists():
        raise RuntimeError(f"source config missing: {SOURCE_CONFIG}")
    text = SOURCE_CONFIG.read_text(encoding="utf-8")
    for expected in ("rtp-min-radius: 1000", "rtp-max-radius: 100000"):
        if expected not in text:
            raise RuntimeError(f"config missing `{expected}`")
    print("[ok] source config defaults for RTP radius")


def main() -> int:
    client = connect_with_retry()
    try:
        run_cmd(client, "plugins", "lkjmcsmp")
        assert_radius_defaults()
        for command in ("menu", "points", "convert", "home", "warp", "team", "tp", "tpa", "tpahere", "tpaccept", "tpdeny", "rtp", "adv"):
            run_cmd(client, f"help {command}", command)
        run_cmd(client, "help lkjmcsmp:tp", "lkjmcsmp:tp")
        return 0
    finally:
        client.stop()


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"smoke failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
