package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

final class CoreMenuService {
    static final String TELEPORT_TITLE = "lkjmcsmp :: teleport";
    static final String HOMES_TITLE = "lkjmcsmp :: homes";
    static final String WARPS_TITLE = "lkjmcsmp :: warps";
    static final String TEAM_TITLE = "lkjmcsmp :: team";
    static final String PICK_TPA_TITLE = "lkjmcsmp :: pick-tpa";
    static final String PICK_TPA_HERE_TITLE = "lkjmcsmp :: pick-tpahere";
    static final String PICK_TP_TITLE = "lkjmcsmp :: pick-tp";
    static final String PICK_INVITE_TITLE = "lkjmcsmp :: pick-invite";
    private final CoreMenuViews views;
    private final Consumer<Player> openRoot;

    CoreMenuService(CoreMenuViews views, Consumer<Player> openRoot) {
        this.views = views;
        this.openRoot = openRoot;
    }

    boolean openFromRoot(Player player, String display) throws Exception {
        return switch (display) {
            case "Teleport" -> open(player, TELEPORT_TITLE);
            case "Homes" -> open(player, HOMES_TITLE);
            case "Warps" -> open(player, WARPS_TITLE);
            case "Team", "Party" -> open(player, TEAM_TITLE);
            default -> false;
        };
    }

    boolean openBack(Player player, String title) throws Exception {
        if (title.equals(PICK_INVITE_TITLE)) {
            open(player, TEAM_TITLE);
            return true;
        }
        if (title.equals(PICK_TPA_TITLE) || title.equals(PICK_TPA_HERE_TITLE) || title.equals(PICK_TP_TITLE)) {
            open(player, TELEPORT_TITLE);
            return true;
        }
        if (handles(title)) {
            openRoot.accept(player);
            return true;
        }
        return false;
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return switch (title) {
            case TELEPORT_TITLE -> handleTeleport(player, display);
            case HOMES_TITLE -> handleHomes(event, player, display);
            case WARPS_TITLE -> handleWarps(player, display);
            case TEAM_TITLE -> handleTeam(player, display);
            case PICK_TPA_TITLE, PICK_TPA_HERE_TITLE, PICK_TP_TITLE, PICK_INVITE_TITLE -> handlePicker(player, title, display);
            default -> false;
        };
    }

    private boolean handles(String title) {
        return title.equals(TELEPORT_TITLE)
                || title.equals(HOMES_TITLE)
                || title.equals(WARPS_TITLE)
                || title.equals(TEAM_TITLE)
                || title.equals(PICK_TPA_TITLE)
                || title.equals(PICK_TPA_HERE_TITLE)
                || title.equals(PICK_TP_TITLE)
                || title.equals(PICK_INVITE_TITLE);
    }

    private boolean handleTeleport(Player player, String display) throws Exception {
        return switch (display) {
            case "Random Teleport" -> command(player, "rtp");
            case "Request Teleport" -> open(player, PICK_TPA_TITLE);
            case "Request Here" -> open(player, PICK_TPA_HERE_TITLE);
            case "Accept Request" -> command(player, "tpaccept");
            case "Deny Request" -> command(player, "tpdeny");
            case "Direct Teleport" -> open(player, PICK_TP_TITLE);
            case "Direct Teleport (Locked)" -> tell(player, "Missing permission: lkjmcsmp.tp.use");
            case "No Pending Request" -> tell(player, "No pending teleport request.");
            case "Refresh" -> open(player, TELEPORT_TITLE);
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
            case "No Homes Set" -> tell(player, "No homes set.");
            case "Refresh" -> open(player, HOMES_TITLE);
            default -> false;
        };
    }

    private boolean handleWarps(Player player, String display) throws Exception {
        if (display.startsWith("Warp :: ")) {
            return command(player, "warp " + display.substring("Warp :: ".length()));
        }
        return switch (display) {
            case "No Warps Set" -> tell(player, "No warps set.");
            case "Refresh" -> open(player, WARPS_TITLE);
            default -> false;
        };
    }

    private boolean handleTeam(Player player, String display) throws Exception {
        return switch (display) {
            case "Team Info" -> open(player, TEAM_TITLE);
            case "Create Team" -> command(player, "team create");
            case "Invite Player" -> open(player, PICK_INVITE_TITLE);
            case "Accept Invite" -> command(player, "team accept");
            case "Leave Team" -> command(player, "team leave");
            case "Team Home" -> command(player, "team home");
            case "Set Team Home" -> command(player, "team sethome");
            case "Disband Team" -> command(player, "team disband");
            case "Refresh" -> open(player, TEAM_TITLE);
            default -> false;
        };
    }

    private boolean handlePicker(Player player, String title, String display) throws Exception {
        if (display.equals("No Players Online")) {
            return tell(player, "No other players online.");
        }
        if (display.equals("Refresh")) {
            return open(player, title);
        }
        if (!display.startsWith("Player :: ")) {
            return false;
        }
        String target = display.substring("Player :: ".length());
        return switch (title) {
            case PICK_TPA_TITLE -> command(player, "tpa " + target);
            case PICK_TPA_HERE_TITLE -> command(player, "tpahere " + target);
            case PICK_TP_TITLE -> command(player, "tp " + target);
            case PICK_INVITE_TITLE -> command(player, "team invite " + target);
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
