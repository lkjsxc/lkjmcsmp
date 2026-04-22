package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.plugin.temporaryend.TemporaryEndManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

final class CoreMenuActions {
    private final CoreMenuViews views;
    private final PointsService pointsService;
    private final TemporaryEndManager temporaryEndManager;
    private final PageTracker tracker = new PageTracker();
    private final PickerActions pickerActions;

    CoreMenuActions(CoreMenuViews views, PointsService pointsService, TemporaryEndManager temporaryEndManager) {
        this.views = views;
        this.pointsService = pointsService;
        this.temporaryEndManager = temporaryEndManager;
        this.pickerActions = new PickerActions(views, tracker);
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return switch (title) {
            case MenuTitles.TELEPORT -> handleTeleport(player, display);
            case MenuTitles.HOMES -> handleHomes(player, display);
            case MenuTitles.HOMES_DELETE -> handleHomesDelete(player, display);
            case MenuTitles.WARPS -> handleWarps(player, display);
            case MenuTitles.TEAM -> handleTeam(player, display);
            case MenuTitles.TEAM_DISBAND_CONFIRM -> handleTeamDisbandConfirm(player, display);
            case MenuTitles.TEMPORARY_END -> handleTemporaryEnd(player, display);
            case MenuTitles.PICK_TPA, MenuTitles.PICK_TPA_HERE, MenuTitles.PICK_TP, MenuTitles.PICK_TP_ACCEPT, MenuTitles.PICK_INVITE ->
                    pickerActions.handle(player, title, display);
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
            default -> false;
        };
    }

    private boolean handleHomes(Player player, String display) throws Exception {
        if (display.startsWith("Home :: ")) {
            return command(player, "home " + display.substring("Home :: ".length()));
        }
        return switch (display) {
            case "Add Current Location" -> {
                command(player, "homes addcurrent");
                views.openHomes(player, tracker.page(player.getUniqueId(), MenuTitles.HOMES));
                yield true;
            }
            case "Delete Homes" -> {
                tracker.setPage(player.getUniqueId(), MenuTitles.HOMES_DELETE, 0);
                views.openHomesDelete(player, 0);
                yield true;
            }
            case "No Homes Set" -> tell(player, "No homes set.");
            case "Page Prev" -> turnPage(player, MenuTitles.HOMES, -1);
            case "Page Next" -> turnPage(player, MenuTitles.HOMES, 1);
            default -> false;
        };
    }

    private boolean handleHomesDelete(Player player, String display) throws Exception {
        if (display.startsWith("Delete Home :: ")) {
            String name = display.substring("Delete Home :: ".length());
            command(player, "delhome " + name);
            views.openHomesDelete(player, tracker.page(player.getUniqueId(), MenuTitles.HOMES_DELETE));
            return true;
        }
        return switch (display) {
            case "Cancel Deletion" -> {
                views.openHomes(player, tracker.page(player.getUniqueId(), MenuTitles.HOMES));
                yield true;
            }
            case "No Homes Set" -> tell(player, "No homes set.");
            case "Page Prev" -> turnPage(player, MenuTitles.HOMES_DELETE, -1);
            case "Page Next" -> turnPage(player, MenuTitles.HOMES_DELETE, 1);
            default -> false;
        };
    }

    private boolean handleWarps(Player player, String display) throws Exception {
        if (display.startsWith("Warp :: ")) {
            return command(player, "warp " + display.substring("Warp :: ".length()));
        }
        return switch (display) {
            case "No Warps Set" -> tell(player, "No warps set.");
            case "Page Prev" -> turnPage(player, MenuTitles.WARPS, -1);
            case "Page Next" -> turnPage(player, MenuTitles.WARPS, 1);
            default -> false;
        };
    }

    private boolean handleTeam(Player player, String display) throws Exception {
        if (display.endsWith("(Locked)")) {
            return tell(player, "Action is locked. Read the item description for details.");
        }
        return switch (display) {
            case "Team Info" -> open(player, MenuTitles.TEAM);
            case "Create Team" -> commandAndRefreshTeam(player, "team create");
            case "Invite Player" -> open(player, MenuTitles.PICK_INVITE);
            case "Accept Invite" -> commandAndRefreshTeam(player, "team accept");
            case "Leave Team" -> commandAndRefreshTeam(player, "team leave");
            case "Team Home" -> command(player, "team home");
            case "Set Team Home" -> commandAndRefreshTeam(player, "team sethome");
            case "Disband Team" -> open(player, MenuTitles.TEAM_DISBAND_CONFIRM);
            default -> false;
        };
    }

    private boolean handleTeamDisbandConfirm(Player player, String display) throws Exception {
        return switch (display) {
            case "Confirm Disband" -> commandAndRefreshTeam(player, "team disband");
            case "Cancel", "Disband Unavailable" -> open(player, MenuTitles.TEAM);
            default -> false;
        };
    }

    private boolean handleTemporaryEnd(Player player, String display) throws Exception {
        if (!display.equals("Purchase")) {
            return false;
        }
        if (temporaryEndManager == null) {
            player.sendMessage("Temporary End is not available.");
            return true;
        }
        var result = pointsService.purchase(player, "temporary_end", 1);
        if (!result.success()) {
            player.sendMessage(result.message());
            views.open(player, MenuTitles.TEMPORARY_END);
            return true;
        }
        temporaryEndManager.purchase(player, player.getLocation());
        player.sendMessage("\u00A7aTemporary End purchased! Nearby players will be transferred.");
        views.open(player, MenuTitles.TEMPORARY_END);
        return true;
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
