package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class CoreMenuActions {
    private final CoreMenuViews views;
    private final Map<UUID, Map<String, Integer>> pagesByPlayer = new ConcurrentHashMap<>();

    CoreMenuActions(CoreMenuViews views) {
        this.views = views;
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return switch (title) {
            case MenuTitles.TELEPORT -> handleTeleport(player, display);
            case MenuTitles.HOMES -> handleHomes(player, display);
            case MenuTitles.HOMES_DELETE -> handleHomesDelete(player, display);
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
                views.openHomes(player, page(player, MenuTitles.HOMES));
                yield true;
            }
            case "Delete Homes" -> {
                setPage(player, MenuTitles.HOMES_DELETE, 0);
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
            views.openHomesDelete(player, page(player, MenuTitles.HOMES_DELETE));
            return true;
        }
        return switch (display) {
            case "Cancel Deletion" -> {
                views.openHomes(player, page(player, MenuTitles.HOMES));
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
        return switch (display) {
            case "Team Info" -> open(player, MenuTitles.TEAM);
            case "Create Team" -> command(player, "team create");
            case "Invite Player" -> open(player, MenuTitles.PICK_INVITE);
            case "Accept Invite" -> command(player, "team accept");
            case "Leave Team" -> command(player, "team leave");
            case "Team Home" -> command(player, "team home");
            case "Set Team Home" -> command(player, "team sethome");
            case "Disband Team" -> command(player, "team disband");
            default -> false;
        };
    }

    private boolean handlePicker(Player player, String title, String display) throws Exception {
        if (display.equals("Refresh")) {
            views.openPicker(player, title, page(player, title));
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

    void clearPlayerState(UUID playerId) {
        pagesByPlayer.remove(playerId);
    }
    private boolean turnPage(Player player, String title, int delta) throws Exception {
        setPage(player, title, page(player, title) + delta);
        return switch (title) {
            case MenuTitles.HOMES -> {
                views.openHomes(player, page(player, title));
                setPage(player, title, MenuPageStateSync.readCurrentPage(player, page(player, title)));
                yield true;
            }
            case MenuTitles.HOMES_DELETE -> {
                views.openHomesDelete(player, page(player, title));
                setPage(player, title, MenuPageStateSync.readCurrentPage(player, page(player, title)));
                yield true;
            }
            case MenuTitles.WARPS -> {
                views.openWarps(player, page(player, title));
                setPage(player, title, MenuPageStateSync.readCurrentPage(player, page(player, title)));
                yield true;
            }
            case MenuTitles.PICK_TPA, MenuTitles.PICK_TPA_HERE, MenuTitles.PICK_TP, MenuTitles.PICK_TP_ACCEPT, MenuTitles.PICK_INVITE -> {
                views.openPicker(player, title, page(player, title));
                setPage(player, title, MenuPageStateSync.readCurrentPage(player, page(player, title)));
                yield true;
            }
            default -> false;
        };
    }

    private int page(Player player, String title) {
        return pagesByPlayer.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(title, 0);
    }

    private void setPage(Player player, String title, int page) {
        pagesByPlayer.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .put(title, Math.max(0, page));
    }
    private boolean open(Player player, String title) throws Exception {
        if (MenuTitles.isPagedMenu(title)) {
            setPage(player, title, 0);
        }
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
