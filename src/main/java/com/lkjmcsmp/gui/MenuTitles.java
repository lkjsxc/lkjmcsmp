package com.lkjmcsmp.gui;

final class MenuTitles {
    private static final String PREFIX = "lkjmcsmp ::";
    static final String ROOT = PREFIX + " menu";
    static final String SHOP = PREFIX + " shop";
    static final String PROGRESS = PREFIX + " progression";
    static final String TELEPORT = PREFIX + " teleport";
    static final String HOMES = PREFIX + " homes";
    static final String WARPS = PREFIX + " warps";
    static final String TEAM = PREFIX + " team";
    static final String PICK_TPA = PREFIX + " pick-tpa";
    static final String PICK_TPA_HERE = PREFIX + " pick-tpahere";
    static final String PICK_TP = PREFIX + " pick-tp";
    static final String PICK_TP_ACCEPT = PREFIX + " pick-tpaccept";
    static final String PICK_INVITE = PREFIX + " pick-invite";

    private MenuTitles() {
    }

    static boolean isPluginMenu(String title) {
        return title != null && title.startsWith(PREFIX);
    }
}
