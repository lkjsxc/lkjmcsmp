package com.lkjmcsmp.achievement;

import com.lkjmcsmp.persistence.AchievementDao;
import com.lkjmcsmp.persistence.PointsDao;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class AchievementService {
    private final AchievementDao achievementDao;
    private final PointsDao pointsDao;
    private final Map<String, AchievementDefinition> definitions;

    public AchievementService(AchievementDao achievementDao, PointsDao pointsDao, ConfigurationSection section) {
        this.achievementDao = achievementDao;
        this.pointsDao = pointsDao;
        this.definitions = parseDefinitions(section);
    }

    private static Map<String, AchievementDefinition> parseDefinitions(ConfigurationSection section) {
        Map<String, AchievementDefinition> map = new LinkedHashMap<>();
        if (section == null) {
            return map;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            map.put(key, new AchievementDefinition(
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
        for (AchievementDefinition definition : definitions.values()) {
            if (!definition.kind().equalsIgnoreCase(kind)) {
                continue;
            }
            AchievementDao.State current = achievementDao.get(playerId, definition.key())
                    .orElse(new AchievementDao.State(AchievementStatus.LOCKED, 0));
            if (current.status() == AchievementStatus.COMPLETED_CLAIMED) {
                continue;
            }
            int nextProgress = current.progress() + Math.max(delta, 0);
            AchievementStatus status = nextProgress >= definition.target()
                    ? AchievementStatus.COMPLETED_UNCLAIMED
                    : AchievementStatus.IN_PROGRESS;
            achievementDao.upsert(playerId, definition.key(), status, nextProgress);
        }
    }

    public Map<String, AchievementView> getViews(UUID playerId) throws Exception {
        Map<String, AchievementView> views = new LinkedHashMap<>();
        for (AchievementDefinition definition : definitions.values()) {
            AchievementDao.State state = achievementDao.get(playerId, definition.key())
                    .orElse(new AchievementDao.State(AchievementStatus.LOCKED, 0));
            views.put(definition.key(), new AchievementView(definition, state.status(), state.progress()));
        }
        return views;
    }

    public Result resetAll(UUID playerId) throws Exception {
        achievementDao.deleteAllForPlayer(playerId);
        return Result.ok("achievements reset");
    }

    public Result claim(UUID playerId, String achievementKey) throws Exception {
        AchievementDefinition definition = definitions.get(achievementKey);
        if (definition == null) {
            return Result.fail("unknown achievement");
        }
        AchievementDao.State state = achievementDao.get(playerId, definition.key())
                .orElse(new AchievementDao.State(AchievementStatus.LOCKED, 0));
        if (state.status() != AchievementStatus.COMPLETED_UNCLAIMED) {
            return Result.fail("achievement not claimable");
        }
        achievementDao.upsert(playerId, definition.key(), AchievementStatus.COMPLETED_CLAIMED, state.progress());
        if (definition.rewardPoints() != 0) {
            pointsDao.addPoints(playerId, definition.rewardPoints(), "ACHIEVEMENT_REWARD", "{\"key\":\"" + definition.key() + "\"}");
        }
        return Result.ok("reward claimed");
    }

    public record AchievementDefinition(String key, String title, String description, String kind, int target, int rewardPoints) {
    }

    public record AchievementView(AchievementDefinition definition, AchievementStatus status, int progress) {
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
