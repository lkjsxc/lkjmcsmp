package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.HomeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class HomeTabCompleter implements TabCompleter {
    private static final List<String> ACTIONS = List.of("create", "list", "go", "delete", "buy-slot");
    private final HomeService homes;

    public HomeTabCompleter(HomeService homes) {
        this.homes = homes;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1) {
            return prefix(merge(ACTIONS, homeNames(player)), args[0]);
        }
        if (args.length == 2 && List.of("go", "delete", "create").contains(args[0].toLowerCase(Locale.ROOT))) {
            return prefix(homeNames(player), args[1]);
        }
        return List.of();
    }

    private List<String> homeNames(Player player) {
        try {
            return homes.list(player.getUniqueId()).stream().map(home -> home.name()).sorted().toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static List<String> merge(List<String> left, List<String> right) {
        return java.util.stream.Stream.concat(left.stream(), right.stream()).distinct().toList();
    }

    private static List<String> prefix(List<String> values, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalized)).toList();
    }
}
