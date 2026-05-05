package com.lkjmcsmp.plugin.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionBarComposerTest {
    @Test
    void idleFormatsCompactPlaytimeAndOnlineCount() {
        long ticks = ((12L * 60L) + 34L) * 60L * 20L;

        assertEquals("Playtime: 12h 34m | Online: 3", ActionBarComposer.idle(ticks, 3));
    }

    @Test
    void idleClampsNegativePlaytimeToZero() {
        assertEquals("Playtime: 0h 0m | Online: 0", ActionBarComposer.idle(-1L, 0));
    }
}
