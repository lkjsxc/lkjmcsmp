package com.lkjmcsmp.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MilestoneStatusTest {
    @Test
    void enumOrderStaysStable() {
        assertEquals("LOCKED", MilestoneStatus.values()[0].name());
        assertEquals("COMPLETED_CLAIMED", MilestoneStatus.values()[3].name());
    }
}
