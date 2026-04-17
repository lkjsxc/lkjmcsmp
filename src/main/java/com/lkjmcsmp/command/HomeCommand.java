package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.plugin.Locations;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class HomeCommand implements CommandExecutor {
    private final HomeService homeService;
    private final TeleportService teleportService;
    private final ProgressionService progressionService;

    public HomeCommand(HomeService homeService, TeleportService teleportService, ProgressionService progressionService) {
        this.homeService = homeService;
        this.teleportService = teleportService;
        this.progressionService = progressionService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                switch (command.getName().toLowerCase()) {
                    case "sethome" -> {
                        var result = homeService.setHome(player, args.length == 0 ? "home" : args[0]);
                        if (result.success()) {
                            progressionService.increment(player.getUniqueId(), "home_set", 1);
                        }
                        player.sendMessage(result.message());
                    }
                    case "delhome" -> {
                        if (args.length == 0) {
                            player.sendMessage("Usage: /delhome <name>");
                            return true;
                        }
                        player.sendMessage(homeService.deleteHome(player.getUniqueId(), args[0]).message());
                    }
                    case "homes" -> listHomesOrAddCurrent(player, args);
                    case "home" -> teleportHome(player, args);
                    default -> {
                        return false;
                    }
                }
            } catch (Exception ex) {
                player.sendMessage("Home command failed: " + ex.getMessage());
            }
            return true;
        }).orElse(true);
    }

    private void teleportHome(org.bukkit.entity.Player player, String[] args) throws Exception {
        String name = args.length == 0 ? "home" : args[0];
        var home = homeService.findHome(player.getUniqueId(), name);
        if (home.isEmpty()) {
            player.sendMessage("Home not found.");
            return;
        }
        var location = Locations.toBukkit(home.get());
        if (location.isEmpty()) {
            player.sendMessage("World is unavailable for that home.");
            return;
        }
        teleportService.teleportToLocation(player, location.get(), "Teleported home.", result -> player.sendMessage(result.message()));
    }

    private void listHomesOrAddCurrent(org.bukkit.entity.Player player, String[] args) throws Exception {
        if (args.length > 0 && (args[0].equalsIgnoreCase("addcurrent") || args[0].equalsIgnoreCase("add-current-location"))) {
            var result = homeService.setAutoHome(player);
            if (result.success()) {
                progressionService.increment(player.getUniqueId(), "home_set", 1);
            }
            player.sendMessage(result.message());
            return;
        }
        player.sendMessage("Homes: " + homeService.list(player.getUniqueId()).stream()
                .map(h -> h.name())
                .toList());
    }
}
