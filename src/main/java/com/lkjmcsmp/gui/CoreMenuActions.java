package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

final class CoreMenuActions {
    private final CoreMenuViews views;
    private final PageTracker tracker = new PageTracker();
    private final PickerActions pickerActions;

    CoreMenuActions(CoreMenuViews views) {
        this.views = views;
        this.pickerActions = new PickerActions(views, tracker);
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        String action = MenuAction.action(event.getCurrentItem());
        if (action.isBlank()) {
            return false;
        }
        String payload = MenuAction.payload(event.getCurrentItem());
        return switch (title) {
            case MenuTitles.TELEPORT -> handleTeleport(player, action);
            case MenuTitles.HOMES -> handleHomes(player, action, payload);
            case MenuTitles.HOMES_DELETE -> handleHomesDelete(player, action, payload);
            case MenuTitles.WARPS -> handleWarps(player, action, payload);
            case MenuTitles.TEAM -> handleTeam(player, action);
            case MenuTitles.TEAM_DISBAND_CONFIRM -> handleTeamDisbandConfirm(player, action);
            case MenuTitles.TP_DECISION -> handleTeleportDecision(player, action, payload);
            case MenuTitles.PICK_TPA, MenuTitles.PICK_TPA_HERE, MenuTitles.PICK_TP, MenuTitles.PICK_TP_ACCEPT, MenuTitles.PICK_INVITE ->
                    pickerActions.handle(player, title, action, payload);
            default -> false;
        };
    }

    private boolean handleTeleportDecision(Player player, String action, String payload) {
        if (!action.equals("tpdecision.accept") && !action.equals("tpdecision.deny")) {
            return false;
        }
        Player requester = org.bukkit.Bukkit.getPlayer(java.util.UUID.fromString(payload));
        if (requester == null) {
            player.sendMessage("Requester is offline.");
            return true;
        }
        player.performCommand((action.endsWith("accept") ? "tpaccept " : "tpdeny ") + requester.getName());
        return true;
    }

    private boolean handleTeleport(Player player, String action) throws Exception {
        return switch (action) {
            case "teleport.rtp" -> command(player, "rtp");
            case "teleport.pick.tpa" -> open(player, MenuTitles.PICK_TPA);
            case "teleport.pick.tpahere" -> open(player, MenuTitles.PICK_TPA_HERE);
            case "teleport.accept" -> command(player, "tpaccept");
            case "teleport.deny" -> command(player, "tpdeny");
            case "teleport.pick.direct" -> open(player, MenuTitles.PICK_TP);
            case "teleport.direct.locked" -> tell(player, "Missing permission: lkjmcsmp.tp.use");
            case "teleport.none" -> tell(player, "No pending teleport request.");
            default -> false;
        };
    }

    private boolean handleHomes(Player player, String action, String payload) throws Exception {
        return switch (action) {
            case "home.teleport" -> command(player, "home " + payload);
            case "home.addcurrent" -> {
                command(player, "homes addcurrent");
                views.openHomes(player, tracker.page(player.getUniqueId(), MenuTitles.HOMES));
                yield true;
            }
            case "home.delete.open" -> {
                tracker.setPage(player.getUniqueId(), MenuTitles.HOMES_DELETE, 0);
                views.openHomesDelete(player, 0);
                yield true;
            }
            case "page.prev" -> turnPage(player, MenuTitles.HOMES, -1);
            case "page.next" -> turnPage(player, MenuTitles.HOMES, 1);
            default -> false;
        };
    }

    private boolean handleHomesDelete(Player player, String action, String payload) throws Exception {
        return switch (action) {
            case "home.delete" -> {
                command(player, "delhome " + payload);
                views.openHomesDelete(player, tracker.page(player.getUniqueId(), MenuTitles.HOMES_DELETE));
                yield true;
            }
            case "home.delete.cancel" -> {
                views.openHomes(player, tracker.page(player.getUniqueId(), MenuTitles.HOMES));
                yield true;
            }
            case "page.prev" -> turnPage(player, MenuTitles.HOMES_DELETE, -1);
            case "page.next" -> turnPage(player, MenuTitles.HOMES_DELETE, 1);
            default -> false;
        };
    }

    private boolean handleWarps(Player player, String action, String payload) throws Exception {
        return switch (action) {
            case "warp.teleport" -> command(player, "warp " + payload);
            case "page.prev" -> turnPage(player, MenuTitles.WARPS, -1);
            case "page.next" -> turnPage(player, MenuTitles.WARPS, 1);
            default -> false;
        };
    }

    private boolean handleTeam(Player player, String action) throws Exception {
        return switch (action) {
            case "locked" -> tell(player, "Action is locked. Read the item description for details.");
            case "team.info" -> open(player, MenuTitles.TEAM);
            case "team.create" -> commandAndRefreshTeam(player, "team create");
            case "team.invite" -> open(player, MenuTitles.PICK_INVITE);
            case "team.accept" -> commandAndRefreshTeam(player, "team accept");
            case "team.leave" -> commandAndRefreshTeam(player, "team leave");
            case "team.home" -> command(player, "team home");
            case "team.sethome" -> commandAndRefreshTeam(player, "team sethome");
            case "team.disband.open" -> open(player, MenuTitles.TEAM_DISBAND_CONFIRM);
            default -> false;
        };
    }

    private boolean handleTeamDisbandConfirm(Player player, String action) throws Exception {
        return switch (action) {
            case "team.disband.confirm" -> commandAndRefreshTeam(player, "team disband");
            case "team.disband.cancel" -> open(player, MenuTitles.TEAM);
            default -> false;
        };
    }

    void clearPlayerState(UUID playerId) {
        tracker.clear(playerId);
    }

    private boolean turnPage(Player player, String title, int delta) throws Exception {
        tracker.setPage(player.getUniqueId(), title, tracker.page(player.getUniqueId(), title) + delta);
        return switch (title) {
            case MenuTitles.HOMES -> {
                views.openHomes(player, tracker.page(player.getUniqueId(), title));
                tracker.setPage(player.getUniqueId(), title, MenuPageStateSync.readCurrentPage(player, tracker.page(player.getUniqueId(), title)));
                yield true;
            }
            case MenuTitles.HOMES_DELETE -> {
                views.openHomesDelete(player, tracker.page(player.getUniqueId(), title));
                tracker.setPage(player.getUniqueId(), title, MenuPageStateSync.readCurrentPage(player, tracker.page(player.getUniqueId(), title)));
                yield true;
            }
            case MenuTitles.WARPS -> {
                views.openWarps(player, tracker.page(player.getUniqueId(), title));
                tracker.setPage(player.getUniqueId(), title, MenuPageStateSync.readCurrentPage(player, tracker.page(player.getUniqueId(), title)));
                yield true;
            }
            default -> false;
        };
    }

    private boolean open(Player player, String title) throws Exception {
        if (MenuTitles.isPagedMenu(title)) {
            tracker.setPage(player.getUniqueId(), title, 0);
        }
        views.open(player, title);
        return true;
    }

    private static boolean tell(Player player, String message) { player.sendMessage(message); return true; }
    private static boolean command(Player player, String command) { player.performCommand(command); return true; }
    private boolean commandAndRefreshTeam(Player player, String command) throws Exception {
        player.performCommand(command);
        views.openTeam(player);
        return true;
    }
}
