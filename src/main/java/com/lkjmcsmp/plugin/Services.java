package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import com.lkjmcsmp.achievement.AchievementService;

public record Services(
        PointsService points,
        HomeService homes,
        WarpService warps,
        PartyService parties,
        TeleportService teleports,
        AchievementService achievement,
        ActionBarRouter hud,
        MenuService menus) {
}
