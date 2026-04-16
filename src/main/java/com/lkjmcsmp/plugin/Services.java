package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.progression.ProgressionService;

public record Services(
        PointsService points,
        HomeService homes,
        WarpService warps,
        PartyService parties,
        TeleportService teleports,
        ProgressionService progression,
        MenuService menus) {
}
