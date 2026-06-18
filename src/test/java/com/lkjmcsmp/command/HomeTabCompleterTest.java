package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HomeTabCompleterTest {
    @TempDir
    Path tempDir;

    @Test
    void directNamespacedHomeCommandCompletesActionsAndHomeNames() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve("homes.db"));
        database.initialize();
        HomeDao dao = new HomeDao(database);
        UUID playerId = UUID.randomUUID();
        dao.upsert(playerId, new NamedLocation("base", "world", 0, 64, 0, 0, 0));
        HomeTabCompleter completer = new HomeTabCompleter(new HomeService(dao, 3));

        List<String> root = completer.onTabComplete(player(playerId), null, "lkjmcsmp:home", new String[] {""});
        List<String> named = completer.onTabComplete(player(playerId), null, "lkjmcsmp:home", new String[] {"b"});

        assertTrue(root.contains("create"));
        assertTrue(root.contains("base"));
        assertEquals(List.of("buy-slot", "base"), named);
    }

    @Test
    void createDeleteAndGoCompleteExistingHomeNames() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve("named-homes.db"));
        database.initialize();
        HomeDao dao = new HomeDao(database);
        UUID playerId = UUID.randomUUID();
        dao.upsert(playerId, new NamedLocation("base", "world", 0, 64, 0, 0, 0));
        HomeTabCompleter completer = new HomeTabCompleter(new HomeService(dao, 3));

        for (String action : List.of("create", "delete", "go")) {
            assertEquals(List.of("base"), completer.onTabComplete(
                    player(playerId), null, "home", new String[] {action, "b"}));
        }
    }

    private static Player player(UUID playerId) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[] {Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "toString" -> "Player(" + playerId + ")";
                    case "hashCode" -> playerId.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return 0;
    }
}
