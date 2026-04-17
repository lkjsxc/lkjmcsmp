package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

final class CoreMenuActions {
    private final CoreMenuViews views;

    CoreMenuActions(CoreMenuViews views) {
        this.views = views;
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return switch (title) {
            case MenuTitles.TELEPORT -> handleTeleport(player, display);
            case MenuTitles.HOMES -> handleHomes(event, player, display);
            case MenuTitles.WARPS -> handleWarps(player, display);
            case MenuTitles.TEAM -> handleTeam(player, display);
            case MenuTitles.PICK_TPA, MenuTitles.PICK_TPA_HERE, MenuTitles.PICK_TP, MenuTitles.PICK_TP_ACCEPT, MenuTitles.PICK_INVITE ->
                    handlePicker(player, title, display);
            default -> false;
        };
    }

    private boolean handleTeleport(Player player, String display) throws Exception {
        return switch (display) {
            case "Random Teleport" -> command(player, "rtp");
            case "Request Teleport" -> open(player, MenuTitles.PICK_TPA);
            case "Request Here" -> open(player, MenuTitles.PICK_TPA_HERE);
            case "Accept Request" -> command(player, "tpaccept");
            case "Deny Request" -> command(player, "tpdeny");
            case "Direct Teleport" -> open(player, MenuTitles.PICK_TP);
            case "Direct Teleport (Locked)" -> tell(player, "Missing permission: lkjmcsmp.tp.use");
            case "No Pending Requests" -> tell(player, "No pending teleport request.");
            case "Refresh" -> open(player, MenuTitles.TELEPORT);
            default -> false;
        };
    }

    private boolean handleHomes(InventoryClickEvent event, Player player, String display) throws Exception {
        if (display.startsWith("Home :: ")) {
            String name = display.substring("Home :: ".length());
            return command(player, (event.getClick().isRightClick() ? "delhome " : "home ") + name);
        }
        return switch (display) {
            case "Set Default Home" -> command(player, "sethome home");
            case "Delete Default Home" -> command(player, "delhome home");
            case "Add Current Location" -> command(player, "homes addcurrent");
            case "No Homes Set" -> tell(player, "No homes set.");
            case "Refresh" -> open(player, MenuTitles.HOMES);
            default -> false;
        };
    }

    private boolean handleWarps(Player player, String display) throws Exception {
        if (display.startsWith("Warp :: ")) {
            return command(player, "warp " + display.substring("Warp :: ".length()));
        }
        return switch (display) {
            case "No Warps Set" -> tell(player, "No warps set.");
            case "Refresh" -> open(player, MenuTitles.WARPS);
            default -> false;
        };
    }

    private boolean handleTeam(Player player, String display) throws Exception {
        return switch (display) {
            case "Team Info" -> open(player, MenuTitles.TEAM);
            case "Create Team" -> command(player, "team create");
            case "Invite Player" -> open(player, MenuTitles.PICK_INVITE);
            case "Accept Invite" -> command(player, "team accept");
            case "Leave Team" -> command(player, "team leave");
            case "Team Home" -> command(player, "team home");
            case "Set Team Home" -> command(player, "team sethome");
            case "Disband Team" -> command(player, "team disband");
            case "Refresh" -> open(player, MenuTitles.TEAM);
            default -> false;
        };
    }

    private boolean handlePicker(Player player, String title, String display) throws Exception {
        if (display.equals("No Players Online")) {
            return tell(player, "No other players online.");
        }
        if (display.equals("No Pending Requests")) {
            return tell(player, "No pending teleport request.");
        }
        if (display.equals("Refresh")) {
            return open(player, title);
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

    private boolean open(Player player, String title) throws Exception {
        views.open(player, title);
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
