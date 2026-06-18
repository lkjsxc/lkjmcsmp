package com.lkjmcsmp.command;

import com.lkjmcsmp.plugin.Services;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class LkjmcsmpTabCompleter implements TabCompleter {
    private static final List<String> ROOT = List.of(
            "help", "menu", "home", "shop", "achievement", "profile",
            "settings", "language", "points", "teleport", "warps", "team");
    private static final List<String> HOME = List.of("list", "go", "set", "addcurrent", "delete", "buy-slot");
    private static final List<String> SHOP = List.of("buy", "convert");
    private static final List<String> SETTINGS = List.of("hotbar", "actionbar");
    private static final List<String> TELEPORT = List.of("rtp", "tpa", "tpahere", "tp", "accept", "deny");
    private static final List<String> TEAM = List.of("create", "invite", "accept", "kick", "leave", "chat", "sethome", "home", "disband", "info");
    private final Services services;

    public LkjmcsmpTabCompleter(Services services) {
        this.services = services;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return prefix(ROOT, args[0]);
        String root = args[0].toLowerCase(Locale.ROOT);
        return switch (root) {
            case "home" -> completeHome(sender, args);
            case "shop" -> completeShop(args);
            case "settings" -> args.length == 2 ? prefix(SETTINGS, args[1]) : List.of();
            case "language" -> args.length == 2 ? prefix(List.copyOf(services.messages().languages().codes()), args[1]) : List.of();
            case "achievement", "ach" -> completeAchievement(args);
            case "teleport" -> args.length == 2 ? prefix(TELEPORT, args[1]) : List.of();
            case "warps" -> completeWarps(sender, args);
            case "team" -> args.length == 2 ? prefix(TEAM, args[1]) : List.of();
            default -> List.of();
        };
    }

    private List<String> completeHome(CommandSender sender, String[] args) {
        if (args.length == 2) return prefix(HOME, args[1]);
        if (!(sender instanceof Player player) || args.length != 3) return List.of();
        if (!List.of("go", "delete").contains(args[1].toLowerCase(Locale.ROOT))) return List.of();
        try {
            return prefix(services.homes().list(player.getUniqueId()).stream().map(h -> h.name()).toList(), args[2]);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<String> completeShop(String[] args) {
        if (args.length == 2) return prefix(SHOP, args[1]);
        if (args.length == 3 && args[1].equalsIgnoreCase("buy")) {
            return prefix(services.points().getShopItems().keySet().stream().sorted().toList(), args[2]);
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("buy")) {
            return prefix(List.of("1", "2", "4", "8", "16", "32", "64"), args[3]);
        }
        return List.of();
    }

    private List<String> completeAchievement(String[] args) {
        if (args.length == 2) return prefix(List.of("list", "claim"), args[1]);
        if (args.length == 3 && args[1].equalsIgnoreCase("claim")) {
            return prefix(List.copyOf(services.achievement().keys()), args[2]);
        }
        return List.of();
    }

    private List<String> completeWarps(CommandSender sender, String[] args) {
        if (args.length == 2) return prefix(List.of("list", "go"), args[1]);
        if (!(sender instanceof Player) || args.length != 3 || !args[1].equalsIgnoreCase("go")) return List.of();
        try {
            return prefix(services.warps().list().stream().map(w -> w.name()).toList(), args[2]);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static List<String> prefix(List<String> values, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalized)).toList();
    }
}
