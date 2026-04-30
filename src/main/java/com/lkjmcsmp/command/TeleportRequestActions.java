package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.TeleportService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

final class TeleportRequestActions {
    private final TeleportService teleportService;

    TeleportRequestActions(TeleportService teleportService) {
        this.teleportService = teleportService;
    }

    void acceptSpecific(Player player, String requesterName) {
        Player requester = requester(player, requesterName);
        if (requester == null) return;
        UUID requesterId = requester.getUniqueId();
        boolean pending = teleportService.pendingFor(player.getUniqueId()).stream()
                .anyMatch(request -> request.from().equals(requesterId));
        if (!pending) {
            player.sendMessage("No pending request from " + requesterName + ".");
            return;
        }
        teleportService.acceptRequest(player, requesterId, result -> {
            player.sendMessage(result.message());
            notifyRequester(player, requesterId, result, "accepted");
        });
    }

    void denySpecific(Player player, String requesterName) {
        Player requester = requester(player, requesterName);
        if (requester == null) return;
        var result = teleportService.denyRequest(player, requester.getUniqueId());
        player.sendMessage(result.message());
        if (result.success()) {
            notifyRequester(player, requester.getUniqueId(), result, "denied");
        }
    }

    void notifyRequester(Player target, UUID requesterId, TeleportService.Result result, String action) {
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null || !requester.isOnline()) return;
        if (result.success()) {
            requester.sendMessage(target.getName() + " " + action + " your teleport request.");
            return;
        }
        requester.sendMessage("Teleport request to " + target.getName() + " failed: " + result.message());
    }

    private static Player requester(Player player, String requesterName) {
        Player requester = Bukkit.getPlayerExact(requesterName);
        if (requester == null) {
            player.sendMessage("Requester is offline.");
        }
        return requester;
    }
}
