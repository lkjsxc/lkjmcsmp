package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;

final class PickerActions {
    private final CoreMenuViews views;
    private final PageTracker tracker;

    PickerActions(CoreMenuViews views, PageTracker tracker) {
        this.views = views;
        this.tracker = tracker;
    }

    boolean handle(Player player, String title, String display) throws Exception {
        if (display.equals("Refresh")) {
            views.openPicker(player, title, tracker.page(player.getUniqueId(), title));
            return true;
        }
        if (display.equals("Page Prev")) {
            return turnPage(player, title, -1);
        }
        if (display.equals("Page Next")) {
            return turnPage(player, title, 1);
        }
        if (display.equals("No Players Online")) {
            return tell(player, "No other players online.");
        }
        if (display.equals("No Pending Requests")) {
            return tell(player, "No pending teleport request.");
        }
        if (!(display.startsWith("Player :: ") || display.startsWith("Requester :: "))) {
            return false;
        }
        String target = display.startsWith("Player :: ")
                ? display.substring("Player :: ".length())
                : display.substring("Requester :: ".length());
        return switch (title) {
            case MenuTitles.PICK_TPA -> command(player, "tpa " + target);
            case MenuTitles.PICK_TPA_HERE -> command(player, "tpahere " + target);
            case MenuTitles.PICK_TP -> command(player, "tp " + target);
            case MenuTitles.PICK_TP_ACCEPT -> command(player, "tpaccept " + target);
            case MenuTitles.PICK_INVITE -> command(player, "team invite " + target);
            default -> false;
        };
    }

    private boolean turnPage(Player player, String title, int delta) throws Exception {
        tracker.setPage(player.getUniqueId(), title, tracker.page(player.getUniqueId(), title) + delta);
        views.openPicker(player, title, tracker.page(player.getUniqueId(), title));
        tracker.setPage(player.getUniqueId(), title, MenuPageStateSync.readCurrentPage(player, tracker.page(player.getUniqueId(), title)));
        return true;
    }

    private static boolean tell(Player player, String message) {
        player.sendMessage(message);
        return true;
    }

    private static boolean command(Player player, String command) {
        player.performCommand(command);
        return true;
    }
}
