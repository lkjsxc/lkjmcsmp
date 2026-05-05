package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.MessageService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PlayerSettingsService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.persistence.AuditDao;
import com.lkjmcsmp.persistence.EconomyOverrideDao;
import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.AchievementDao;
import com.lkjmcsmp.persistence.PartyDao;
import com.lkjmcsmp.persistence.PlayerSettingsDao;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import com.lkjmcsmp.persistence.WarpDao;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionBootstrap;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Duration;
import java.util.Objects;

public final class LkjmcsmpPlugin extends JavaPlugin {
    private Services services;
    private SchedulerBridge schedulerBridge;
    private TemporaryDimensionManager temporaryDimensionManager;
    private SqliteDatabase database;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            saveResource("shop.yml", false);
            saveResource("achievements.yml", false);
            Services initialized = initializeServices();
            CommandRegistry.registerAll(this, initialized, temporaryDimensionManager);
            ListenerRegistry.registerAll(this, initialized, schedulerBridge, temporaryDimensionManager);
            ProxyRuntimeValidator.validate(this);
            this.services = initialized;
            getLogger().info("lkjmcsmp enabled");
        } catch (Exception ex) {
            getLogger().severe("Failed to enable plugin: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (services != null && services.hud() != null) {
            services.hud().stop();
        }
        if (schedulerBridge != null) {
            schedulerBridge.cancelTasks();
        }
        if (database != null) {
            database.close();
        }
        getLogger().info("lkjmcsmp disabled");
    }

    private Services initializeServices() throws Exception {
        FileConfiguration config = getConfig();
        String dbPath = config.getString("database.file", "plugins/lkjmcsmp/lkjmcsmp.db");
        this.database = new SqliteDatabase(new File(getDataFolder().getParentFile().getParentFile(), dbPath).toPath());
        this.database.initialize();

        PointsDao pointsDao = new PointsDao(database);
        EconomyOverrideDao economyOverrideDao = new EconomyOverrideDao(database);
        HomeDao homeDao = new HomeDao(database);
        WarpDao warpDao = new WarpDao(database);
        PartyDao partyDao = new PartyDao(database);
        AchievementDao achievementDao = new AchievementDao(database);
        AuditDao auditDao = new AuditDao(database);
        MessageService.LanguageRegistry languages = MessageService.loadRegistry(this);
        PlayerSettingsService settingsService = new PlayerSettingsService(
                new PlayerSettingsDao(database), languages.codes(), languages.defaultLanguage());
        MessageService messageService = new MessageService(this, settingsService, languages);

        FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shop.yml"));
        FileConfiguration achievementsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "achievements.yml"));

        this.schedulerBridge = new FoliaSchedulerBridge(this);
        PointsService pointsService = new PointsService(
                pointsDao,
                economyOverrideDao,
                auditDao,
                shopConfig.getConfigurationSection("items"),
                config.getBoolean("economy.allow-partial-convert", false),
                config.getInt("economy.max-convert-per-op", 4096));
        ActionBarRouter actionBarHudService = new ActionBarRouter(schedulerBridge);
        AchievementService achievementService = new AchievementService(
                achievementDao,
                pointsDao,
                achievementsConfig.getConfigurationSection("achievements"));

        HomeService homeService = new HomeService(homeDao, config.getInt("homes.max-per-player", 3));
        WarpService warpService = new WarpService(warpDao);
        PartyService partyService = new PartyService(
                partyDao,
                Duration.ofSeconds(config.getInt("party.invite-timeout-seconds", 60)));
        TeleportService teleportService = new TeleportService(
                schedulerBridge,
                Duration.ofSeconds(config.getInt("teleport.request-timeout-seconds", 60)),
                Duration.ofSeconds(config.getInt("teleport.rtp-cooldown-seconds", 60)),
                Duration.ofSeconds(config.getInt("teleport.stability-delay-seconds", 5)),
                config.getDouble("teleport.stability-radius-blocks", 1.0D),
                config.getInt("teleport.rtp-min-radius", 1000),
                config.getInt("teleport.rtp-max-radius", 100000),
                config.getInt("teleport.rtp-attempts", 10),
                Objects.requireNonNull(config.getStringList("teleport.rtp-world-whitelist")),
                actionBarHudService);
        this.temporaryDimensionManager = TemporaryDimensionBootstrap.bootstrap(
                this, schedulerBridge, pointsDao, new TemporaryDimensionDao(database), config, actionBarHudService);
        pointsService.registerEffect("temporary_dimension_pass", temporaryDimensionManager);

        MenuService menuService = new MenuService(
                pointsService,
                achievementService,
                actionBarHudService,
                homeService,
                warpService,
                partyService,
                teleportService,
                settingsService,
                messageService,
                schedulerBridge);
        return new Services(
                pointsService,
                homeService,
                warpService,
                partyService,
                teleportService,
                settingsService,
                messageService,
                achievementService,
                actionBarHudService,
                menuService);
    }

    public Services services() {
        return services;
    }
}
