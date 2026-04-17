package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

final class CoreMenuService {
    private final CoreMenuViews views;
    private final CoreMenuActions actions;
    private final Consumer<Player> openRoot;

    CoreMenuService(CoreMenuViews views, Consumer<Player> openRoot) {
        this.views = views;
        this.actions = new CoreMenuActions(views);
        this.openRoot = openRoot;
    }

    boolean openFromRoot(Player player, String display) throws Exception {
        return switch (display) {
            case "Teleport" -> open(player, MenuTitles.TELEPORT);
            case "Homes" -> open(player, MenuTitles.HOMES);
            case "Warps" -> open(player, MenuTitles.WARPS);
            case "Team", "Party" -> open(player, MenuTitles.TEAM);
            default -> false;
        };
    }

    boolean openBack(Player player, String title) throws Exception {
        if (title.equals(MenuTitles.PICK_INVITE)) {
            open(player, MenuTitles.TEAM);
            return true;
        }
        if (title.equals(MenuTitles.PICK_TPA)
                || title.equals(MenuTitles.PICK_TPA_HERE)
                || title.equals(MenuTitles.PICK_TP)
                || title.equals(MenuTitles.PICK_TP_ACCEPT)) {
            open(player, MenuTitles.TELEPORT);
            return true;
        }
        if (handles(title)) {
            openRoot.accept(player);
            return true;
        }
        return false;
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return actions.handleClick(event, player, title, display);
    }

    private static boolean handles(String title) {
        return title.equals(MenuTitles.TELEPORT)
                || title.equals(MenuTitles.HOMES)
                || title.equals(MenuTitles.WARPS)
                || title.equals(MenuTitles.TEAM)
                || title.equals(MenuTitles.PICK_TPA)
                || title.equals(MenuTitles.PICK_TPA_HERE)
                || title.equals(MenuTitles.PICK_TP)
                || title.equals(MenuTitles.PICK_TP_ACCEPT)
                || title.equals(MenuTitles.PICK_INVITE);
    }

    boolean open(Player player, String title) throws Exception {
        views.open(player, title);
        return true;
    }
}
