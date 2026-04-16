package com.lkjmcsmp.progression;

import com.lkjmcsmp.persistence.MilestoneDao;
import com.lkjmcsmp.persistence.PointsDao;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ProgressionService {
    private final MilestoneDao milestoneDao;
    private final PointsDao pointsDao;
    private final Map<String, MilestoneDefinition> definitions;

    public ProgressionService(MilestoneDao milestoneDao, PointsDao pointsDao, ConfigurationSection section) {
        this.milestoneDao = milestoneDao;
        this.pointsDao = pointsDao;
        this.definitions = parseDefinitions(section);
    }

    private static Map<String, MilestoneDefinition> parseDefinitions(ConfigurationSection section) {
        Map<String, MilestoneDefinition> map = new LinkedHashMap<>();
        if (section == null) {
            return map;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            map.put(key, new MilestoneDefinition(
                    key,
                    entry.getString("title", key),
                    entry.getString("description", entry.getString("title", key)),
                    entry.getString("kind", "custom"),
                    entry.getInt("target", 1),
                    entry.getInt("reward-points", 0)));
        }
        return map;
    }

    public void increment(UUID playerId, String kind, int delta) throws Exception {
        for (MilestoneDefinition definition : definitions.values()) {
            if (!definition.kind().equalsIgnoreCase(kind)) {
                continue;
            }
            MilestoneDao.State current = milestoneDao.get(playerId, definition.key())
                    .orElse(new MilestoneDao.State(MilestoneStatus.LOCKED, 0));
            if (current.status() == MilestoneStatus.COMPLETED_CLAIMED) {
                continue;
            }
            int nextProgress = current.progress() + Math.max(delta, 0);
            MilestoneStatus status = nextProgress >= definition.target()
                    ? MilestoneStatus.COMPLETED_UNCLAIMED
                    : MilestoneStatus.IN_PROGRESS;
            milestoneDao.upsert(playerId, definition.key(), status, nextProgress);
        }
    }

    public Map<String, MilestoneView> getViews(UUID playerId) throws Exception {
        Map<String, MilestoneView> views = new LinkedHashMap<>();
        for (MilestoneDefinition definition : definitions.values()) {
            MilestoneDao.State state = milestoneDao.get(playerId, definition.key())
                    .orElse(new MilestoneDao.State(MilestoneStatus.LOCKED, 0));
            views.put(definition.key(), new MilestoneView(definition, state.status(), state.progress()));
        }
        return views;
    }

    public Result claim(UUID playerId, String milestoneKey) throws Exception {
        MilestoneDefinition definition = definitions.get(milestoneKey);
        if (definition == null) {
            return Result.fail("unknown milestone");
        }
        MilestoneDao.State state = milestoneDao.get(playerId, definition.key())
                .orElse(new MilestoneDao.State(MilestoneStatus.LOCKED, 0));
        if (state.status() != MilestoneStatus.COMPLETED_UNCLAIMED) {
            return Result.fail("milestone not claimable");
        }
        milestoneDao.upsert(playerId, definition.key(), MilestoneStatus.COMPLETED_CLAIMED, state.progress());
        if (definition.rewardPoints() != 0) {
            pointsDao.addPoints(playerId, definition.rewardPoints(), "MILESTONE_REWARD", "{\"key\":\"" + definition.key() + "\"}");
        }
        return Result.ok("reward claimed");
    }

    public record MilestoneDefinition(String key, String title, String description, String kind, int target, int rewardPoints) {
    }

    public record MilestoneView(MilestoneDefinition definition, MilestoneStatus status, int progress) {
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
