package com.lkjmcsmp.achievement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AchievementStatusTest {
    @Test
    void enumOrderStaysStable() {
        assertEquals("LOCKED", AchievementStatus.values()[0].name());
        assertEquals("COMPLETED_CLAIMED", AchievementStatus.values()[3].name());
    }
}
