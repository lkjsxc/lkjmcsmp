package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.persistence.PartyDao;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PartyService {
    private final PartyDao partyDao;
    private final Duration inviteTimeout;

    public PartyService(PartyDao partyDao, Duration inviteTimeout) {
        this.partyDao = partyDao;
        this.inviteTimeout = inviteTimeout;
    }

    public Result create(UUID leaderId, String partyName) throws Exception {
        if (partyName == null || partyName.isBlank()) {
            return Result.fail("party name is required");
        }
        if (partyDao.findPartyId(leaderId).isPresent()) {
            return Result.fail("already in party");
        }
        String partyId = normalize(partyName) + "-" + UUID.randomUUID().toString().substring(0, 8);
        partyDao.createParty(partyId, leaderId);
        return Result.ok("party created");
    }

    public Result invite(UUID leaderId, UUID targetId) throws Exception {
        Optional<String> partyId = partyDao.findPartyId(leaderId);
        if (partyId.isEmpty()) {
            return Result.fail("not in a party");
        }
        if (!isLeader(leaderId)) {
            return Result.fail("only leader can invite");
        }
        if (partyDao.findPartyId(targetId).isPresent()) {
            return Result.fail("target already in a party");
        }
        partyDao.upsertInvite(partyId.get(), targetId, leaderId, Instant.now().plus(inviteTimeout));
        return Result.ok("invite sent");
    }

    public Result accept(UUID targetId) throws Exception {
        Optional<PartyDao.InviteRecord> record = partyDao.getInvite(targetId);
        if (record.isEmpty()) {
            return Result.fail("no pending invite");
        }
        if (record.get().expiresAt().isBefore(Instant.now())) {
            partyDao.deleteInvite(targetId);
            return Result.fail("invite expired");
        }
        if (partyDao.findPartyId(targetId).isPresent()) {
            return Result.fail("already in party");
        }
        partyDao.joinParty(record.get().partyId(), targetId);
        partyDao.deleteInvite(targetId);
        return Result.ok("joined party");
    }

    public Result leave(UUID playerId) throws Exception {
        Optional<String> partyId = partyDao.findPartyId(playerId);
        if (partyId.isEmpty()) {
            return Result.fail("not in a party");
        }
        if (isLeader(playerId)) {
            List<UUID> members = partyDao.listMembers(partyId.get());
            if (members.size() > 1) {
                return Result.fail("leader must disband or transfer before leaving");
            }
        }
        partyDao.removeMember(playerId);
        return Result.ok("left party");
    }

    public Result disband(UUID actorId) throws Exception {
        Optional<String> partyId = partyDao.findPartyId(actorId);
        if (partyId.isEmpty()) {
            return Result.fail("not in a party");
        }
        if (!isLeader(actorId)) {
            return Result.fail("only leader can disband");
        }
        partyDao.disband(partyId.get());
        return Result.ok("party disbanded");
    }

    public Result kick(UUID actorId, UUID targetId) throws Exception {
        Optional<String> actorParty = partyDao.findPartyId(actorId);
        Optional<String> targetParty = partyDao.findPartyId(targetId);
        if (actorParty.isEmpty() || targetParty.isEmpty() || !actorParty.get().equals(targetParty.get())) {
            return Result.fail("target not in your party");
        }
        if (!isLeader(actorId)) {
            return Result.fail("only leader can kick");
        }
        partyDao.removeMember(targetId);
        return Result.ok("member kicked");
    }

    public Optional<String> getPartyId(UUID playerId) throws Exception {
        return partyDao.findPartyId(playerId);
    }

    public Result setPartyHome(Player actor) throws Exception {
        Optional<String> partyId = partyDao.findPartyId(actor.getUniqueId());
        if (partyId.isEmpty()) {
            return Result.fail("not in a party");
        }
        if (!isLeader(actor.getUniqueId())) {
            return Result.fail("only leader can set party home");
        }
        Location loc = actor.getLocation();
        partyDao.upsertHome(partyId.get(), new NamedLocation(
                "party",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()));
        return Result.ok("party home updated");
    }

    public Optional<NamedLocation> getPartyHome(UUID playerId) throws Exception {
        Optional<String> partyId = partyDao.findPartyId(playerId);
        if (partyId.isEmpty()) {
            return Optional.empty();
        }
        return partyDao.getHome(partyId.get());
    }

    public List<UUID> listMembers(UUID playerId) throws Exception {
        Optional<String> partyId = partyDao.findPartyId(playerId);
        return partyId.map(id -> {
            try {
                return partyDao.listMembers(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).orElse(List.of());
    }

    public boolean isLeader(UUID playerId) throws Exception {
        return partyDao.findRole(playerId).map("leader"::equalsIgnoreCase).orElse(false);
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase().replace(' ', '-');
    }

    public record Result(boolean success, String message) {
        public static Result ok(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
