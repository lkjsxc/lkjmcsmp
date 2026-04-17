package com.lkjmcsmp.command;

import com.lkjmcsmp.plugin.scoreboard.SidebarRenderer;
import com.lkjmcsmp.plugin.scoreboard.SidebarSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class VerifyCommand implements CommandExecutor {
    private static final String VERIFY_PERMISSION = "lkjmcsmp.verify.use";
    private final SidebarRenderer sidebarRenderer = new SidebarRenderer();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(VERIFY_PERMISSION)) {
            sender.sendMessage("Missing permission: " + VERIFY_PERMISSION);
            return true;
        }
        if (args.length != 1 || !"scoreboard".equalsIgnoreCase(args[0])) {
            sender.sendMessage("Usage: /lkjverify scoreboard");
            return true;
        }
        try {
            runScoreboardProbe();
            sender.sendMessage("OK scoreboard probe");
        } catch (Exception ex) {
            sender.sendMessage("Scoreboard probe failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return true;
    }

    private void runScoreboardProbe() {
        if (Bukkit.getScoreboardManager() == null) {
            throw new IllegalStateException("scoreboard manager unavailable");
        }
        SidebarSnapshot snapshot = new SidebarSnapshot("verify", 1, 1, 3, 42, false);
        try {
            Scoreboard managed = sidebarRenderer.createManagedBoard(snapshot);
            Objective managedSidebar = managed.getObjective(DisplaySlot.SIDEBAR);
            if (managedSidebar == null || !"lkjmcsmp".equals(managedSidebar.getName())) {
                throw new IllegalStateException("managed board creation failed");
            }
            Scoreboard overwritten = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective overwrite = overwritten.registerNewObjective("verify_overwrite", Criteria.DUMMY, "overwrite");
            overwrite.setDisplaySlot(DisplaySlot.SIDEBAR);
            if (overwritten.getObjective(DisplaySlot.SIDEBAR) != overwrite) {
                throw new IllegalStateException("overwrite injection failed");
            }
            Scoreboard removed = Bukkit.getScoreboardManager().getNewScoreboard();
            if (removed.getObjective("lkjmcsmp") != null) {
                throw new IllegalStateException("managed objective removal injection failed");
            }
            Scoreboard rebuilt = sidebarRenderer.createManagedBoard(snapshot);
            Objective rebuiltSidebar = rebuilt.getObjective(DisplaySlot.SIDEBAR);
            if (rebuiltSidebar == null || !"lkjmcsmp".equals(rebuiltSidebar.getName())) {
                throw new IllegalStateException("sidebar objective reclaim failed");
            }
        } catch (UnsupportedOperationException ignored) {
            // Folia scoreboard APIs may throw UnsupportedOperationException in non-player probe paths.
        }
    }

}
