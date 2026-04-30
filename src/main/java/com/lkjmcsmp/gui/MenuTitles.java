package com.lkjmcsmp.gui;

final class MenuTitles {
    private static final String PREFIX = "lkjmcsmp ::";
    static final String ROOT = PREFIX + " menu";
    static final String SHOP = PREFIX + " shop";
    static final String SHOP_DETAIL = PREFIX + " shop-detail";
    static final String ACHIEVEMENT = PREFIX + " achievement";
    static final String TELEPORT = PREFIX + " teleport";
    static final String HOMES = PREFIX + " homes";
    static final String HOMES_DELETE = PREFIX + " homes-delete";
    static final String WARPS = PREFIX + " warps";
    static final String TEAM = PREFIX + " team";
    static final String TEAM_DISBAND_CONFIRM = PREFIX + " team-disband-confirm";
    static final String SETTINGS = PREFIX + " settings";
    static final String LANGUAGE = PREFIX + " language";
    static final String TP_DECISION = PREFIX + " tp-decision";
    static final String PICK_TPA = PREFIX + " pick-tpa";
    static final String PICK_TPA_HERE = PREFIX + " pick-tpahere";
    static final String PICK_TP = PREFIX + " pick-tp";
    static final String PICK_TP_ACCEPT = PREFIX + " pick-tpaccept";
    static final String PICK_INVITE = PREFIX + " pick-invite";
    static final String PROFILE = PREFIX + " profile";

    private MenuTitles() {
    }

    static boolean isPluginMenu(String title) {
        return title != null && title.startsWith(PREFIX);
    }

    static boolean isPagedMenu(String title) {
        return HOMES.equals(title)
                || HOMES_DELETE.equals(title)
                || WARPS.equals(title)
                || PICK_TPA.equals(title)
                || PICK_TPA_HERE.equals(title)
                || PICK_TP.equals(title)
                || PICK_TP_ACCEPT.equals(title)
                || PICK_INVITE.equals(title)
                || SHOP.equals(title)
                || ACHIEVEMENT.equals(title);
    }
}
