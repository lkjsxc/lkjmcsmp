package com.lkjmcsmp.plugin;

import com.lkjmcsmp.command.AdvCommand;
import com.lkjmcsmp.command.HomeCommand;
import com.lkjmcsmp.command.MenuCommand;
import com.lkjmcsmp.command.PointsCommand;
import com.lkjmcsmp.command.TeamCommand;
import com.lkjmcsmp.command.TeleportCommand;
import com.lkjmcsmp.command.VerifyCommand;
import com.lkjmcsmp.command.WarpCommand;
import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.gui.HotbarMenuListener;
import com.lkjmcsmp.gui.HotbarMenuService;
import com.lkjmcsmp.gui.MenuListener;
import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.persistence.AuditDao;
import com.lkjmcsmp.persistence.EconomyOverrideDao;
import com.lkjmcsmp.persistence.FirstJoinDao;
import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.MilestoneDao;
import com.lkjmcsmp.persistence.PartyDao;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import com.lkjmcsmp.persistence.WarpDao;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Duration;
import java.util.Objects;

public final class LkjmcsmpPlugin extends JavaPlugin {
    private Services services;
    private SchedulerBridge schedulerBridge;
    private FirstJoinDao firstJoinDao;
    private SmpScoreboardService scoreboardService;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            saveResource("shop.yml", false);
            saveResource("milestones.yml", false);
            Services initialized = initializeServices();
            registerCommands(initialized);
            registerListeners(initialized);
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
        if (scoreboardService != null) {
            scoreboardService.stop();
        }
        getLogger().info("lkjmcsmp disabled");
    }

    private Services initializeServices() throws Exception {
        FileConfiguration config = getConfig();
        String dbPath = config.getString("database.file", "plugins/lkjmcsmp/lkjmcsmp.db");
        SqliteDatabase database = new SqliteDatabase(new File(getDataFolder().getParentFile().getParentFile(), dbPath).toPath());
        database.initialize();

        PointsDao pointsDao = new PointsDao(database);
        EconomyOverrideDao economyOverrideDao = new EconomyOverrideDao(database);
        HomeDao homeDao = new HomeDao(database);
        WarpDao warpDao = new WarpDao(database);
        PartyDao partyDao = new PartyDao(database);
        MilestoneDao milestoneDao = new MilestoneDao(database);
        AuditDao auditDao = new AuditDao(database);

        FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shop.yml"));
        FileConfiguration milestonesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "milestones.yml"));

        this.schedulerBridge = new FoliaSchedulerBridge(this);
        PointsService pointsService = new PointsService(
                pointsDao,
                economyOverrideDao,
                auditDao,
                shopConfig.getConfigurationSection("items"),
                config.getBoolean("economy.allow-partial-convert", false),
                config.getInt("economy.max-convert-per-op", 4096));
        ProgressionService progressionService = new ProgressionService(
                milestoneDao,
                pointsDao,
                milestonesConfig.getConfigurationSection("milestones"));

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
                Objects.requireNonNull(config.getStringList("teleport.rtp-world-whitelist")));
        this.firstJoinDao = new FirstJoinDao(database);

        MenuService menuService = new MenuService(
                pointsService,
                progressionService,
                homeService,
                warpService,
                partyService,
                teleportService,
                schedulerBridge);
        return new Services(pointsService, homeService, warpService, partyService, teleportService, progressionService, menuService);
    }

    private void registerCommands(Services services) {
        register("menu", new MenuCommand(services.menus()));
        register("points", new PointsCommand(services.points(), services.menus(), services.progression()));
        register("convert", new PointsCommand(services.points(), services.menus(), services.progression()));
        register("shop", new PointsCommand(services.points(), services.menus(), services.progression()));
        register("home", new HomeCommand(services.homes(), services.teleports(), services.progression()));
        register("sethome", new HomeCommand(services.homes(), services.teleports(), services.progression()));
        register("delhome", new HomeCommand(services.homes(), services.teleports(), services.progression()));
        register("homes", new HomeCommand(services.homes(), services.teleports(), services.progression()));
        register("warp", new WarpCommand(services.warps(), services.teleports(), services.progression()));
        register("setwarp", new WarpCommand(services.warps(), services.teleports(), services.progression()));
        register("delwarp", new WarpCommand(services.warps(), services.teleports(), services.progression()));
        register("warps", new WarpCommand(services.warps(), services.teleports(), services.progression()));
        register("team", new TeamCommand(services.parties(), services.teleports(), services.progression()));
        register("tp", new TeleportCommand(services.teleports(), services.menus(), services.progression()));
        register("tpa", new TeleportCommand(services.teleports(), services.menus(), services.progression()));
        register("tpahere", new TeleportCommand(services.teleports(), services.menus(), services.progression()));
        register("tpaccept", new TeleportCommand(services.teleports(), services.menus(), services.progression()));
        register("tpdeny", new TeleportCommand(services.teleports(), services.menus(), services.progression()));
        register("rtp", new TeleportCommand(services.teleports(), services.menus(), services.progression()));
        register("adv", new AdvCommand(services.progression(), services.menus()));
        register("lkjverify", new VerifyCommand());
    }

    private void registerListeners(Services initialized) {
        getServer().getPluginManager().registerEvents(new MenuListener(initialized.menus()), this);
        HotbarMenuService hotbarMenuService = new HotbarMenuService(this, initialized.menus());
        getServer().getPluginManager().registerEvents(new HotbarMenuListener(hotbarMenuService), this);
        for (var online : getServer().getOnlinePlayers()) {
            hotbarMenuService.install(online);
        }
        getServer().getPluginManager().registerEvents(new TeleportCommandOverrideListener(getLogger()), this);
        this.scoreboardService = new SmpScoreboardService(schedulerBridge, initialized.points(), getLogger());
        getServer().getPluginManager().registerEvents(new SmpScoreboardListener(scoreboardService), this);
        scoreboardService.start();
        if (getConfig().getBoolean("teleport.first-join.enabled", true)) {
            String firstJoinWorld = Objects.requireNonNull(getConfig().getString("teleport.first-join.world", ""));
            getServer().getPluginManager().registerEvents(
                    new FirstJoinTeleportListener(initialized.teleports(), firstJoinDao, schedulerBridge, firstJoinWorld, getLogger()),
                    this);
        }
    }

    private void register(String command, CommandExecutor executor) {
        Objects.requireNonNull(getCommand(command), "Command missing in plugin.yml: " + command).setExecutor(executor);
    }

    public Services services() {
        return services;
    }
}
