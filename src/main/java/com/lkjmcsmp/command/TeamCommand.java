package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.plugin.Locations;
import com.lkjmcsmp.plugin.SchedulerBridge;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public final class TeamCommand implements CommandExecutor {
    private final PartyService partyService;
    private final SchedulerBridge schedulerBridge;
    private final ProgressionService progressionService;

    public TeamCommand(PartyService partyService, SchedulerBridge schedulerBridge, ProgressionService progressionService) {
        this.partyService = partyService;
        this.schedulerBridge = schedulerBridge;
        this.progressionService = progressionService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            if (args.length == 0) {
                player.sendMessage("Usage: /team <create|invite|accept|kick|leave|chat|sethome|home|disband|info>");
                return true;
            }
            try {
                handle(player, args);
            } catch (Exception ex) {
                player.sendMessage("Team command failed: " + ex.getMessage());
            }
            return true;
        }).orElse(true);
    }

    private void handle(Player player, String[] args) throws Exception {
        switch (args[0].toLowerCase()) {
            case "create" -> player.sendMessage(partyService.create(player.getUniqueId(), args.length > 1 ? args[1] : "party").message());
            case "invite" -> invite(player, args);
            case "accept" -> {
                var result = partyService.accept(player.getUniqueId());
                if (result.success()) {
                    progressionService.increment(player.getUniqueId(), "party_join", 1);
                }
                player.sendMessage(result.message());
            }
            case "kick" -> kick(player, args);
            case "leave" -> player.sendMessage(partyService.leave(player.getUniqueId()).message());
            case "disband" -> player.sendMessage(partyService.disband(player.getUniqueId()).message());
            case "sethome" -> player.sendMessage(partyService.setPartyHome(player).message());
            case "home" -> teleportPartyHome(player);
            case "info" -> player.sendMessage("Party: " + partyService.getPartyId(player.getUniqueId()).orElse("<none>"));
            case "chat" -> teamChat(player, args);
            default -> player.sendMessage("Unknown subcommand.");
        }
    }

    private void invite(Player player, String[] args) throws Exception {
        if (args.length < 2) {
            player.sendMessage("Usage: /team invite <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Target offline.");
            return;
        }
        player.sendMessage(partyService.invite(player.getUniqueId(), target.getUniqueId()).message());
    }

    private void kick(Player player, String[] args) throws Exception {
        if (args.length < 2) {
            player.sendMessage("Usage: /team kick <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Target offline.");
            return;
        }
        player.sendMessage(partyService.kick(player.getUniqueId(), target.getUniqueId()).message());
    }

    private void teleportPartyHome(Player player) throws Exception {
        var home = partyService.getPartyHome(player.getUniqueId());
        if (home.isEmpty()) {
            player.sendMessage("Party home not set.");
            return;
        }
        var location = Locations.toBukkit(home.get());
        if (location.isEmpty()) {
            player.sendMessage("Party home world unavailable.");
            return;
        }
        schedulerBridge.runPlayerTask(player, () -> player.teleport(location.get()));
        player.sendMessage("Teleported to party home.");
    }

    private void teamChat(Player player, String[] args) throws Exception {
        if (args.length < 2) {
            player.sendMessage("Usage: /team chat <message>");
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        for (UUID memberId : partyService.listMembers(player.getUniqueId())) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("[Party] " + player.getName() + ": " + message);
            }
        }
    }
}
