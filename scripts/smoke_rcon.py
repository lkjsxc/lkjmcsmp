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
SOURCE_MILESTONES = WORKSPACE / "src" / "main" / "resources" / "milestones.yml"
SOURCE_MENU_TITLES = WORKSPACE / "src" / "main" / "java" / "com" / "lkjmcsmp" / "gui" / "MenuTitles.java"
SOURCE_SLOT_MAP = WORKSPACE / "docs" / "product" / "gui" / "slot-maps.md"
SOURCE_SHOP_VIEW = WORKSPACE / "src" / "main" / "java" / "com" / "lkjmcsmp" / "gui" / "TopLevelMenuViews.java"
SOURCE_SCOREBOARD_RENDERER = WORKSPACE / "src" / "main" / "java" / "com" / "lkjmcsmp" / "plugin" / "scoreboard" / "SidebarRenderer.java"


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


def assert_progression_and_menu_contract_markers():
    if not SOURCE_MILESTONES.exists():
        raise RuntimeError(f"source milestones missing: {SOURCE_MILESTONES}")
    milestones_text = SOURCE_MILESTONES.read_text(encoding="utf-8")
    required_milestones = (
        "first_shop_purchase:",
        "shop_bulk_buyer:",
        "first_tpa_request:",
        "first_rtp:",
    )
    for expected in required_milestones:
        if expected not in milestones_text:
            raise RuntimeError(f"milestones missing `{expected}`")
    if not SOURCE_MENU_TITLES.exists():
        raise RuntimeError(f"menu titles source missing: {SOURCE_MENU_TITLES}")
    menu_titles_text = SOURCE_MENU_TITLES.read_text(encoding="utf-8")
    for expected in ("SHOP_DETAIL", "HOMES_DELETE"):
        if expected not in menu_titles_text:
            raise RuntimeError(f"menu titles missing `{expected}`")
    print("[ok] progression/menu contract markers present")


def assert_gui_slot_map_alignment_markers():
    if not SOURCE_SLOT_MAP.exists():
        raise RuntimeError(f"slot map doc missing: {SOURCE_SLOT_MAP}")
    slot_map_text = SOURCE_SLOT_MAP.read_text(encoding="utf-8")
    for expected in (
        "## Shop Detail Menu",
        "`20`: Set Quantity 1",
        "`21`: Quantity -8",
        "`22`: Quantity -1",
        "`23`: Quantity Display",
        "`24`: Quantity +1",
        "`25`: Quantity +8",
        "`26`: Set Quantity 64",
    ):
        if expected not in slot_map_text:
            raise RuntimeError(f"slot map missing `{expected}`")
    if not SOURCE_SHOP_VIEW.exists():
        raise RuntimeError(f"shop view source missing: {SOURCE_SHOP_VIEW}")
    shop_view_text = SOURCE_SHOP_VIEW.read_text(encoding="utf-8")
    for expected in (
        "inventory.setItem(20, MenuItems.named(Material.CLOCK, \"Set Quantity 1\"));",
        "inventory.setItem(21, MenuItems.named(Material.RED_DYE, \"Quantity -8\"));",
        "inventory.setItem(22, MenuItems.named(Material.REDSTONE, \"Quantity -1\"));",
        "inventory.setItem(23, MenuItems.named(",
        "inventory.setItem(24, MenuItems.named(Material.LIME_DYE, \"Quantity +1\"));",
        "inventory.setItem(25, MenuItems.named(Material.EMERALD, \"Quantity +8\"));",
        "inventory.setItem(26, MenuItems.named(Material.CLOCK, \"Set Quantity 64\"));",
    ):
        if expected not in shop_view_text:
            raise RuntimeError(f"shop detail alignment missing `{expected}`")
    print("[ok] gui slot-map markers present")


def assert_scoreboard_runtime_probe_markers():
    if not SOURCE_SCOREBOARD_RENDERER.exists():
        raise RuntimeError(f"scoreboard renderer source missing: {SOURCE_SCOREBOARD_RENDERER}")
    renderer_text = SOURCE_SCOREBOARD_RENDERER.read_text(encoding="utf-8")
    for expected in ("ONLINE_ENTRY", "POINTS_ENTRY", "createManagedBoard", "DisplaySlot.SIDEBAR"):
        if expected not in renderer_text:
            raise RuntimeError(f"scoreboard renderer missing `{expected}`")
    print("[ok] scoreboard renderer markers present")


def main() -> int:
    client = connect_with_retry()
    try:
        run_cmd(client, "plugins", "lkjmcsmp")
        assert_radius_defaults()
        assert_progression_and_menu_contract_markers()
        assert_gui_slot_map_alignment_markers()
        assert_scoreboard_runtime_probe_markers()
        for command in ("menu", "points", "convert", "home", "warp", "team", "tp", "tpa", "tpahere", "tpaccept", "tpdeny", "rtp", "adv", "lkjverify"):
            run_cmd(client, f"help {command}", command)
        run_cmd(client, "help lkjmcsmp:tp", "lkjmcsmp:tp")
        run_cmd(client, "lkjverify scoreboard", "OK scoreboard probe")
        return 0
    finally:
        client.stop()


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"smoke failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
