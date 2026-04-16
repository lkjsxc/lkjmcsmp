package com.lkjmcsmp.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class CommandUtil {
    private CommandUtil() {
    }

    public static Optional<Player> requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return Optional.of(player);
        }
        sender.sendMessage("This command can only be used by players.");
        return Optional.empty();
    }

    public static boolean requirePermission(CommandSender sender, String node) {
        if (sender.hasPermission(node)) {
            return true;
        }
        sender.sendMessage("Missing permission: " + node);
        return false;
    }
}
