package com.lkjmcsmp.plugin.hud;

public record ActionBarMessage(
        ActionBarPriority priority,
        String text,
        String source,
        long expiresAtMs,
        long createdAtMs) {

    public ActionBarMessage(ActionBarPriority priority, String text, String source, long expiresAtMs) {
        this(priority, text, source, expiresAtMs, System.currentTimeMillis());
    }

    public boolean expired() {
        return expiresAtMs > 0 && System.currentTimeMillis() > expiresAtMs;
    }
}
