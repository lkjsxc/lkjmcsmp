package com.lkjmcsmp.plugin.hud;

import com.lkjmcsmp.domain.MessageService;
import com.lkjmcsmp.domain.PlayerSettingsService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.persistence.AuditDao;
import com.lkjmcsmp.persistence.EconomyOverrideDao;
import com.lkjmcsmp.persistence.PlayerSettingsDao;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActionBarRouterTest {
    private static final String IDLE_TEMPLATE =
            "Points: {points} | Playtime: {hours}h {minutes}m | Online: {online}";

    @TempDir
    Path tempDir;

    @Test
    void disabledPlayersDoNotReceiveIdleRenders() throws Exception {
        Fixture fixture = fixture();
        UUID playerId = UUID.randomUUID();
        List<String> sent = new ArrayList<>();
        Player player = player(playerId, sent);

        fixture.settings.setActionBarEnabled(playerId, false);
        fixture.router.renderOnce(player);

        assertTrue(sent.isEmpty());
    }

    @Test
    void disabledPlayersDoNotReceiveOverlayMessages() throws Exception {
        Fixture fixture = fixture();
        UUID playerId = UUID.randomUUID();
        List<String> sent = new ArrayList<>();
        Player player = player(playerId, sent);

        fixture.settings.setActionBarEnabled(playerId, false);
        fixture.router.onTeleportCountdown(player, 3);
        fixture.router.renderOnce(player);

        assertTrue(sent.isEmpty());
    }

    @Test
    void reEnabledPlayersResumePluginHudRenderingWithoutBlankClear() throws Exception {
        Fixture fixture = fixture();
        UUID playerId = UUID.randomUUID();
        List<String> sent = new ArrayList<>();
        Player player = player(playerId, sent);

        fixture.router.renderOnce(player);
        fixture.settings.setActionBarEnabled(playerId, false);
        fixture.router.onActionBarPreferenceChanged(player);
        fixture.router.renderOnce(player);
        fixture.settings.setActionBarEnabled(playerId, true);
        fixture.router.onActionBarPreferenceChanged(player);
        fixture.router.renderOnce(player);

        assertEquals(List.of(idleText(), idleText()), sent);
    }

    private Fixture fixture() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        PlayerSettingsService settings = new PlayerSettingsService(
                new PlayerSettingsDao(database), Set.of("en"), "en");
        MessageService messages = new MessageService(
                settings,
                new MessageService.LanguageRegistry("en", Map.of("en", "English")),
                Map.of("en", Map.of("hud.idle", IDLE_TEMPLATE)));
        PointsService points = new PointsService(
                new PointsDao(database),
                new EconomyOverrideDao(database),
                new AuditDao(database),
                null,
                false,
                4096);
        return new Fixture(new ActionBarRouter(new ImmediateScheduler(), points, messages, settings), settings);
    }

    private static Player player(UUID playerId, List<String> actionBars) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[] {Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "isOnline" -> true;
                    case "getStatistic" -> args != null && args[0] == Statistic.PLAY_ONE_MINUTE ? 0 : 0;
                    case "sendActionBar" -> {
                        if (args != null && args.length == 1 && args[0] instanceof String text) {
                            actionBars.add(text);
                        }
                        yield null;
                    }
                    case "toString" -> "TestPlayer";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        return null;
    }

    private static String idleText() {
        return "Points: 0 | Playtime: 0h 0m | Online: 0";
    }

    private record Fixture(ActionBarRouter router, PlayerSettingsService settings) {
    }

    private static final class ImmediateScheduler implements SchedulerBridge {
        @Override
        public void runPlayerTask(Player player, Runnable runnable) {
            runnable.run();
        }

        @Override
        public void runPlayerDelayedTask(Player player, long delayTicks, Runnable runnable) {
        }

        @Override
        public void runRegionTask(Location location, Runnable runnable) {
            runnable.run();
        }

        @Override
        public void runGlobalTask(Runnable runnable) {
            runnable.run();
        }

        @Override
        public void runGlobalDelayedTask(long delayTicks, Runnable runnable) {
        }

        @Override
        public void runAsyncTask(Runnable runnable) {
            runnable.run();
        }

        @Override
        public void cancelTasks() {
        }
    }
}
