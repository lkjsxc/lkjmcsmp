package com.lkjmcsmp.plugin.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionBarComposerTest {
    private static final String TEMPLATE = "Points: {points} | Playtime: {hours}h {minutes}m | Online: {online}";

    @Test
    void idleFormatsCompactPlaytimeAndOnlineCount() {
        long ticks = ((12L * 60L) + 34L) * 60L * 20L;

        assertEquals("Points: 250 | Playtime: 12h 34m | Online: 3",
                ActionBarComposer.idle(250, ticks, 3, TEMPLATE));
    }

    @Test
    void idleClampsNegativePlaytimeToZero() {
        assertEquals("Points: 0 | Playtime: 0h 0m | Online: 0",
                ActionBarComposer.idle(0, -1L, 0, TEMPLATE));
    }
}
