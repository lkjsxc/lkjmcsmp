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
SOURCE_ACHIEVEMENTS = WORKSPACE / "src" / "main" / "resources" / "achievements.yml"
SOURCE_MENU_TITLES = WORKSPACE / "src" / "main" / "java" / "com" / "lkjmcsmp" / "gui" / "MenuTitles.java"
SOURCE_SLOT_MAP = WORKSPACE / "docs" / "product" / "gui" / "slot-maps.md"
SOURCE_SHOP_VIEW = WORKSPACE / "src" / "main" / "java" / "com" / "lkjmcsmp" / "gui" / "TopLevelMenuViews.java"
SOURCE_TEAM_VIEW = WORKSPACE / "src" / "main" / "java" / "com" / "lkjmcsmp" / "gui" / "TeamMenuView.java"
SOURCE_ACTIONBAR_HUD = WORKSPACE / "src" / "main" / "java" / "com" / "lkjmcsmp" / "plugin" / "hud" / "ActionBarHudService.java"


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


def assert_achievement_and_menu_contract_markers():
    if not SOURCE_ACHIEVEMENTS.exists():
        raise RuntimeError(f"source achievements missing: {SOURCE_ACHIEVEMENTS}")
    achievements_text = SOURCE_ACHIEVEMENTS.read_text(encoding="utf-8")
    required_achievements = (
        "first_shop_purchase:",
        "shop_bulk_buyer:",
        "first_tpa_request:",
        "first_rtp:",
        "cobblestone_legend:",
    )
    for expected in required_achievements:
        if expected not in achievements_text:
            raise RuntimeError(f"achievements missing `{expected}`")
    if not SOURCE_MENU_TITLES.exists():
        raise RuntimeError(f"menu titles source missing: {SOURCE_MENU_TITLES}")
    menu_titles_text = SOURCE_MENU_TITLES.read_text(encoding="utf-8")
    for expected in ("SHOP_DETAIL", "HOMES_DELETE", "TEAM_DISBAND_CONFIRM"):
        if expected not in menu_titles_text:
            raise RuntimeError(f"menu titles missing `{expected}`")
    print("[ok] achievement/menu contract markers present")


def assert_gui_slot_map_alignment_markers():
    if not SOURCE_SLOT_MAP.exists():
        raise RuntimeError(f"slot map doc missing: {SOURCE_SLOT_MAP}")
    slot_map_text = SOURCE_SLOT_MAP.read_text(encoding="utf-8")
    for expected in (
        "## Shop Detail Menu",
        "`20`: Buy x1",
        "`21`: Buy x2",
        "`22`: Buy x4",
        "`23`: Buy x8",
        "`24`: Buy x16",
        "`25`: Buy x32",
        "`26`: Buy x64",
        "## Team Disband Confirm Menu",
        "`30`: Confirm Disband",
        "`32`: Cancel",
    ):
        if expected not in slot_map_text:
            raise RuntimeError(f"slot map missing `{expected}`")
    if not SOURCE_SHOP_VIEW.exists():
        raise RuntimeError(f"shop view source missing: {SOURCE_SHOP_VIEW}")
    shop_view_text = SOURCE_SHOP_VIEW.read_text(encoding="utf-8")
    for expected in (
        "quantityItem(selected.points(), points, 1)",
        "quantityItem(selected.points(), points, 2)",
        "quantityItem(selected.points(), points, 4)",
        "quantityItem(selected.points(), points, 8)",
        "quantityItem(selected.points(), points, 16)",
        "quantityItem(selected.points(), points, 32)",
        "quantityItem(selected.points(), points, 64)",
    ):
        if expected not in shop_view_text:
            raise RuntimeError(f"shop detail alignment missing `{expected}`")
    if not SOURCE_TEAM_VIEW.exists():
        raise RuntimeError(f"team view source missing: {SOURCE_TEAM_VIEW}")
    team_view_text = SOURCE_TEAM_VIEW.read_text(encoding="utf-8")
    for expected in ("MenuTitles.TEAM_DISBAND_CONFIRM", "\"Confirm Disband\"", "\"Cancel\""):
        if expected not in team_view_text:
            raise RuntimeError(f"team disband confirmation missing `{expected}`")
    print("[ok] gui slot-map markers present")


def assert_actionbar_hud_markers():
    if not SOURCE_ACTIONBAR_HUD.exists():
        raise RuntimeError(f"actionbar hud source missing: {SOURCE_ACTIONBAR_HUD}")
    hud_text = SOURCE_ACTIONBAR_HUD.read_text(encoding="utf-8")
    for expected in ("COMBAT_TTL_TICKS", "onTeleportCountdown", "onTeleportResult", "refreshIdle"):
        if expected not in hud_text:
            raise RuntimeError(f"actionbar hud source missing `{expected}`")
    print("[ok] actionbar hud markers present")


def main() -> int:
    client = connect_with_retry()
    try:
        run_cmd(client, "plugins", "lkjmcsmp")
        assert_radius_defaults()
        assert_achievement_and_menu_contract_markers()
        assert_gui_slot_map_alignment_markers()
        assert_actionbar_hud_markers()
        for command in ("menu", "points", "convert", "home", "warp", "team", "tp", "tpa", "tpahere", "tpaccept", "tpdeny", "rtp", "achievement", "ach"):
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
