package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.HomeSlotPurchaseService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.plugin.Locations;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class HomeCommand implements CommandExecutor {
    private final HomeService homeService;
    private final HomeSlotPurchaseService homeSlotPurchases;
    private final TeleportService teleportService;
    private final AchievementService achievementService;
    private final ActionBarRouter actionBarHudService;

    public HomeCommand(
            HomeService homeService,
            HomeSlotPurchaseService homeSlotPurchases,
            TeleportService teleportService,
            AchievementService achievementService,
            ActionBarRouter actionBarHudService) {
        this.homeService = homeService;
        this.homeSlotPurchases = homeSlotPurchases;
        this.teleportService = teleportService;
        this.achievementService = achievementService;
        this.actionBarHudService = actionBarHudService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                switch (command.getName().toLowerCase()) {
                    case "delhome" -> {
                        if (args.length == 0) {
                            player.sendMessage("Usage: /delhome <name>");
                            return true;
                        }
                        player.sendMessage(homeService.deleteHome(player.getUniqueId(), args[0]).message());
                    }
                    case "homes" -> listHomesOrBuySlot(player, args);
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
        if (args.length > 0 && args[0].equalsIgnoreCase("create")) {
            createHome(player, args);
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            listHomes(player);
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("go")) {
            args = java.util.Arrays.copyOfRange(args, 1, args.length);
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("delete")) {
            deleteHome(player, args);
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("buy-slot")) {
            buySlot(player);
            return;
        }
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

    private void listHomesOrBuySlot(org.bukkit.entity.Player player, String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("buy-slot")) {
            buySlot(player);
            return;
        }
        listHomes(player);
    }

    private void createHome(org.bukkit.entity.Player player, String[] args) throws Exception {
        var result = args.length > 1 ? homeService.setHome(player, args[1]) : homeService.setAutoHome(player);
        if (result.success()) {
            achievementService.increment(player.getUniqueId(), "home_set", 1);
        }
        player.sendMessage(result.message());
    }

    private void deleteHome(org.bukkit.entity.Player player, String[] args) throws Exception {
        if (args.length < 2) {
            player.sendMessage("Usage: /home delete <name>");
            return;
        }
        player.sendMessage(homeService.deleteHome(player.getUniqueId(), args[1]).message());
    }

    private void buySlot(org.bukkit.entity.Player player) throws Exception {
        var result = homeSlotPurchases.purchaseNext(player);
        if (result.success()) {
            actionBarHudService.refreshIdle(player);
        }
        player.sendMessage(result.message());
    }

    private void listHomes(org.bukkit.entity.Player player) throws Exception {
        player.sendMessage("Homes: " + homeService.list(player.getUniqueId()).stream()
                .map(h -> h.name())
                .toList());
    }
}
