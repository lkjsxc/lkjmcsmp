package com.lkjmcsmp.command;

import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class AdvCommand implements CommandExecutor {
    private final ProgressionService progressionService;
    private final MenuService menuService;

    public AdvCommand(ProgressionService progressionService, MenuService menuService) {
        this.progressionService = progressionService;
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                if (args.length == 2 && args[0].equalsIgnoreCase("claim")) {
                    player.sendMessage(progressionService.claim(player.getUniqueId(), args[1]).message());
                    return true;
                }
                if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                    for (var entry : progressionService.getViews(player.getUniqueId()).entrySet()) {
                        var view = entry.getValue();
                        player.sendMessage(
                                entry.getKey()
                                        + " [" + view.status().name() + "] "
                                        + view.progress() + "/" + view.definition().target()
                                        + " :: " + view.definition().description());
                    }
                    return true;
                }
                menuService.openProgress(player);
            } catch (Exception ex) {
                player.sendMessage("Adv command failed: " + ex.getMessage());
            }
            return true;
        }).orElse(true);
    }
}
