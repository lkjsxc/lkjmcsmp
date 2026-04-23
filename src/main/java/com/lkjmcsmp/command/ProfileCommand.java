package com.lkjmcsmp.command;

import com.lkjmcsmp.gui.MenuService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ProfileCommand implements CommandExecutor {
    private final MenuService menuService;

    public ProfileCommand(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            menuService.openProfile(player);
            return true;
        }).orElse(true);
    }
}
