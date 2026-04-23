package com.lkjmcsmp.plugin.hud;

final class ActionBarComposer {
    private static final int HP_SEGMENTS = 16;

    private ActionBarComposer() {
    }

    static String idle(int points, int onlineCount) {
        return "Cobblestone Points: " + points + " | Online: " + onlineCount;
    }

    static String combat(String targetName, double currentHealth, double maxHealth) {
        String hpBar = buildHpBar(currentHealth, maxHealth);
        return "\u00A7e" + targetName + "\u00A7f " + hpBar;
    }

    static String teleportCountdown(long secondsRemaining) {
        return "\u00A7bTeleport in \u00A7f" + secondsRemaining + "\u00A7bs";
    }

    static String teleportResult(boolean success, String message) {
        String prefix = success ? "\u00A7aTeleport complete" : "\u00A7cTeleport failed";
        return prefix + "\u00A77: \u00A7f" + message;
    }

    static String shopPurchase(String itemKey, int cost) {
        return "\u00A7aPurchased \u00A7f" + itemKey + "\u00A7a for \u00A7f" + cost + "\u00A7a Cobblestone Points";
    }

    static String temporaryDimension(long remainingSeconds) {
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        return "\u00A75Temporary Dimension \u00A7f" + minutes + "m " + seconds + "s remaining";
    }

    private static String buildHpBar(double currentHealth, double maxHealth) {
        if (maxHealth <= 0) {
            return "\u00A77" + "|".repeat(HP_SEGMENTS);
        }
        double ratio = Math.max(0.0D, Math.min(1.0D, currentHealth / maxHealth));
        int filled = (int) Math.round(ratio * HP_SEGMENTS);
        int empty = Math.max(0, HP_SEGMENTS - filled);
        return "\u00A7a" + "|".repeat(filled) + "\u00A77" + "|".repeat(empty);
    }
}
