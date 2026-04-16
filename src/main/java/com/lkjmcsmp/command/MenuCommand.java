package com.lkjmcsmp.command;

import com.lkjmcsmp.gui.MenuService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class MenuCommand implements CommandExecutor {
    private final MenuService menuService;

    public MenuCommand(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            menuService.openRoot(player);
            return true;
        }).orElse(true);
    }
}
