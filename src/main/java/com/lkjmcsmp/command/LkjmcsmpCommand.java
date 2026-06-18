package com.lkjmcsmp.command;

import com.lkjmcsmp.plugin.Services;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class LkjmcsmpCommand implements CommandExecutor {
    private static final List<String> ROOT = List.of(
            "help", "menu", "home", "shop", "achievement", "profile",
            "settings", "language", "points", "teleport", "warps", "team");
    private final Services services;

    public LkjmcsmpCommand(Services services) {
        this.services = services;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, args.length > 1 ? args[1] : "");
            return true;
        }
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                handle(player, args);
            } catch (Exception ex) {
                player.sendMessage("lkjmcsmp command failed: " + ex.getMessage());
            }
            return true;
        }).orElse(true);
    }

    private void handle(Player player, String[] args) throws Exception {
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "menu" -> services.menus().openRoot(player);
            case "home" -> handleHome(player, tail(args));
            case "shop" -> handleShop(player, tail(args));
            case "warps" -> handleWarps(player, tail(args));
            case "achievement", "ach" -> handleAchievement(player, tail(args));
            case "profile" -> services.menus().openProfile(player);
            case "settings" -> handleSettings(player, tail(args));
            case "language" -> handleLanguage(player, tail(args));
            case "points" -> player.performCommand("points");
            case "teleport" -> handleTeleport(player, tail(args));
            case "team" -> handleTeam(player, tail(args));
            default -> {
                player.sendMessage("Unknown lkjmcsmp subcommand: " + args[0]);
                sendHelp(player, "");
            }
        }
    }

    private void handleHome(Player player, String[] args) throws Exception {
        if (args.length == 0) {
            sendHelp(player, "home");
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> player.performCommand("homes");
            case "go" -> player.performCommand("home " + (args.length > 1 ? args[1] : "home"));
            case "create" -> player.performCommand("home create" + suffix(args, 1));
            case "delete" -> player.performCommand(args.length > 1 ? "delhome " + args[1] : "delhome");
            case "buy-slot" -> buyHomeSlot(player);
            default -> sendHelp(player, "home");
        }
    }

    private void buyHomeSlot(Player player) throws Exception {
        var result = services.homeSlotPurchases().purchaseNext(player);
        if (result.success()) {
            services.hud().refreshIdle(player);
        }
        player.sendMessage(result.message());
    }

    private void handleShop(Player player, String[] args) throws Exception {
        if (args.length == 0) {
            services.menus().openShop(player);
            return;
        }
        if (args[0].equalsIgnoreCase("buy")) {
            player.performCommand("shop buy" + suffix(args, 1));
            return;
        }
        if (args[0].equalsIgnoreCase("convert")) {
            var result = services.points().convertAllCobblestone(player);
            if (result.success()) {
                services.achievement().increment(player.getUniqueId(), "convert_amount", result.amount());
                services.hud().refreshIdle(player);
            }
            player.sendMessage(result.message());
            return;
        }
        sendHelp(player, "shop");
    }

    private void handleWarps(Player player, String[] args) throws Exception {
        if (args.length == 0) {
            services.menus().openWarps(player);
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> player.performCommand("warps");
            case "go" -> player.performCommand(args.length > 1 ? "warp " + args[1] : "warp");
            default -> sendHelp(player, "warps");
        }
    }

    private void handleAchievement(Player player, String[] args) {
        if (args.length == 0) {
            services.menus().openAchievement(player);
            return;
        }
        player.performCommand("achievement " + String.join(" ", args));
    }

    private void handleLanguage(Player player, String[] args) throws Exception {
        if (args.length == 0) {
            services.menus().openLanguage(player);
            return;
        }
        services.settings().setLanguage(player.getUniqueId(), args[0]);
        String label = services.messages().languages().languages().getOrDefault(args[0], args[0]);
        player.sendMessage(services.messages().get(player, "settings.language.changed", "language", label));
    }

    private void handleSettings(Player player, String[] args) throws Exception {
        if (args.length == 0) {
            services.menus().openSettings(player);
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "hotbar" -> {
                boolean enabled = services.settings().toggleHotbarMenu(player.getUniqueId()).hotbarMenuEnabled();
                player.sendMessage(services.messages().get(player,
                        enabled ? "settings.hotbar.enabled" : "settings.hotbar.disabled"));
            }
            case "actionbar" -> {
                boolean enabled = services.settings().toggleActionBar(player.getUniqueId()).actionBarEnabled();
                services.hud().onActionBarPreferenceChanged(player);
                player.sendMessage(services.messages().get(player,
                        enabled ? "settings.actionbar.enabled" : "settings.actionbar.disabled"));
            }
            default -> sendHelp(player, "settings");
        }
    }

    private void handleTeleport(Player player, String[] args) throws Exception {
        if (args.length == 0) {
            services.menus().openTeleport(player);
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        String command = switch (action) {
            case "rtp" -> "rtp" + suffix(args, 1);
            case "tpa", "tpahere", "tp" -> action + suffix(args, 1);
            case "accept" -> "tpaccept" + suffix(args, 1);
            case "deny" -> "tpdeny" + suffix(args, 1);
            default -> "";
        };
        if (command.isBlank()) sendHelp(player, "teleport"); else player.performCommand(command);
    }

    private void handleTeam(Player player, String[] args) throws Exception {
        if (args.length == 0) services.menus().openTeam(player);
        else player.performCommand("team " + String.join(" ", args));
    }

    private void sendHelp(CommandSender sender, String topic) {
        if (topic.equalsIgnoreCase("home")) {
            sender.sendMessage("/lkjmcsmp home <list|go|create|delete|buy-slot>");
        } else if (topic.equalsIgnoreCase("shop")) {
            sender.sendMessage("/lkjmcsmp shop [buy <item> [quantity]|convert]");
        } else if (topic.equalsIgnoreCase("settings")) {
            sender.sendMessage("/lkjmcsmp settings [hotbar|actionbar]");
        } else if (topic.equalsIgnoreCase("teleport")) {
            sender.sendMessage("/lkjmcsmp teleport [rtp|tpa|tpahere|tp|accept|deny] [...]");
        } else if (topic.equalsIgnoreCase("warps")) {
            sender.sendMessage("/lkjmcsmp warps [list|go <name>]");
        } else {
            sender.sendMessage("/lkjmcsmp <" + String.join("|", ROOT) + ">");
        }
    }

    private static String[] tail(String[] args) { return Arrays.copyOfRange(args, 1, args.length); }

    private static String suffix(String[] args, int start) {
        return args.length <= start ? "" : " " + String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }
}
