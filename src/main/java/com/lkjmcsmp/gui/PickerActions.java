package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;

final class PickerActions {
    private final CoreMenuViews views;
    private final PageTracker tracker;

    PickerActions(CoreMenuViews views, PageTracker tracker) {
        this.views = views;
        this.tracker = tracker;
    }

    boolean handle(Player player, String title, String action, String payload) throws Exception {
        if (action.equals("picker.refresh")) {
            views.openPicker(player, title, tracker.page(player.getUniqueId(), title));
            return true;
        }
        if (action.equals("page.prev")) {
            return turnPage(player, title, -1);
        }
        if (action.equals("page.next")) {
            return turnPage(player, title, 1);
        }
        if (!(action.equals("picker.player") || action.equals("picker.requester"))) {
            return false;
        }
        return switch (title) {
            case MenuTitles.PICK_TPA -> command(player, "tpa " + payload);
            case MenuTitles.PICK_TPA_HERE -> command(player, "tpahere " + payload);
            case MenuTitles.PICK_TP -> command(player, "tp " + payload);
            case MenuTitles.PICK_TP_ACCEPT -> command(player, "tpaccept " + payload);
            case MenuTitles.PICK_INVITE -> command(player, "team invite " + payload);
            default -> false;
        };
    }

    private boolean turnPage(Player player, String title, int delta) throws Exception {
        tracker.setPage(player.getUniqueId(), title, tracker.page(player.getUniqueId(), title) + delta);
        views.openPicker(player, title, tracker.page(player.getUniqueId(), title));
        tracker.setPage(player.getUniqueId(), title, MenuPageStateSync.readCurrentPage(player, tracker.page(player.getUniqueId(), title)));
        return true;
    }

    private static boolean command(Player player, String command) {
        player.performCommand(command);
        return true;
    }
}
