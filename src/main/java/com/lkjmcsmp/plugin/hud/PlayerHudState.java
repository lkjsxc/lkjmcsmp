package com.lkjmcsmp.plugin.hud;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class PlayerHudState {
    private final Map<String, ActionBarMessage> messages = new ConcurrentHashMap<>();
    private volatile String lastSent = null;

    void put(ActionBarMessage message) {
        messages.put(message.source(), message);
    }

    void remove(String source) {
        messages.remove(source);
    }

    ActionBarMessage get(String source) {
        return messages.get(source);
    }

    void clear() {
        messages.clear();
        lastSent = null;
    }

    void clearLastSent() {
        lastSent = null;
    }

    String computeEffective() {
        ActionBarMessage best = null;
        for (var it = messages.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            ActionBarMessage msg = entry.getValue();
            if (msg.expired()) {
                it.remove();
                continue;
            }
            if (best == null || msg.priority().ordinal() < best.priority().ordinal()
                    || (msg.priority() == best.priority()
                    && msg.createdAtMs() > best.createdAtMs())) {
                best = msg;
            }
        }
        return best != null ? best.text() : null;
    }

    boolean shouldSend(String text) {
        if (text == null) {
            text = "";
        }
        if (text.equals(lastSent)) {
            return false;
        }
        lastSent = text;
        return true;
    }
}
