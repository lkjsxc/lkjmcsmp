package com.lkjmcsmp.plugin.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerHudStateTest {

    @Test
    void computeEffectiveReturnsHighestPriority() {
        PlayerHudState state = new PlayerHudState();
        state.put(new ActionBarMessage(ActionBarPriority.IDLE, "idle", "idle", -1));
        state.put(new ActionBarMessage(ActionBarPriority.COMBAT, "combat", "combat", System.currentTimeMillis() + 5000));
        assertEquals("combat", state.computeEffective());
    }

    @Test
    void computeEffectiveReturnsIdleWhenOverlaysExpire() {
        PlayerHudState state = new PlayerHudState();
        state.put(new ActionBarMessage(ActionBarPriority.IDLE, "idle", "idle", -1));
        state.put(new ActionBarMessage(ActionBarPriority.COMBAT, "combat", "combat", System.currentTimeMillis() - 1000));
        assertEquals("idle", state.computeEffective());
    }

    @Test
    void shouldSendSuppressesDuplicates() {
        PlayerHudState state = new PlayerHudState();
        assertTrue(state.shouldSend("text1"));
        assertFalse(state.shouldSend("text1"));
        assertTrue(state.shouldSend("text2"));
    }

    @Test
    void clearLastSentAllowsResend() {
        PlayerHudState state = new PlayerHudState();
        assertTrue(state.shouldSend("text1"));
        assertFalse(state.shouldSend("text1"));
        state.clearLastSent();
        assertTrue(state.shouldSend("text1"));
    }

    @Test
    void expiredMessagesAreRemoved() {
        PlayerHudState state = new PlayerHudState();
        state.put(new ActionBarMessage(ActionBarPriority.COMBAT, "expired", "combat", System.currentTimeMillis() - 1000));
        assertNull(state.computeEffective());
        assertNull(state.get("combat"));
    }

    @Test
    void equalPriorityTieBreaksByNewest() {
        PlayerHudState state = new PlayerHudState();
        long now = System.currentTimeMillis();
        state.put(new ActionBarMessage(ActionBarPriority.COMBAT, "older", "combat1", now + 5000, now - 1000));
        state.put(new ActionBarMessage(ActionBarPriority.COMBAT, "newer", "combat2", now + 5000, now));
        assertEquals("newer", state.computeEffective());
    }

    @Test
    void actionBarMessageExpired() {
        long past = System.currentTimeMillis() - 1000;
        long future = System.currentTimeMillis() + 5000;
        assertTrue(new ActionBarMessage(ActionBarPriority.IDLE, "", "", past).expired());
        assertFalse(new ActionBarMessage(ActionBarPriority.IDLE, "", "", future).expired());
        assertFalse(new ActionBarMessage(ActionBarPriority.IDLE, "", "", -1).expired());
    }
}
