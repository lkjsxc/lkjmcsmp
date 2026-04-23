package com.lkjmcsmp.plugin.hud;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ActionBarHudListener implements Listener {
    private final ActionBarRouter actionBarHudService;

    public ActionBarHudListener(ActionBarRouter actionBarHudService) {
        this.actionBarHudService = actionBarHudService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        actionBarHudService.incrementOnlineCount();
        actionBarHudService.onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        actionBarHudService.decrementOnlineCount();
        actionBarHudService.onPlayerQuit(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || !attacker.isOnline()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        double maxHealth = 20.0D;
        if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        }
        double currentHealth = Math.max(0.0D, target.getHealth() - event.getFinalDamage());
        actionBarHudService.onCombatHit(attacker, target.getName(), currentHealth, maxHealth);
    }

    private static Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
