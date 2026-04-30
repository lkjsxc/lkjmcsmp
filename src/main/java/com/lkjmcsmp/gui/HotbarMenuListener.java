package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class HotbarMenuListener implements Listener {
    private final HotbarMenuService hotbarMenuService;

    public HotbarMenuListener(HotbarMenuService hotbarMenuService) {
        this.hotbarMenuService = hotbarMenuService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        hotbarMenuService.install(event.getPlayer());
        hotbarMenuService.syncSoon(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        player.getScheduler().runDelayed(hotbarMenuService.plugin(), task -> {
            hotbarMenuService.install(player);
            player.updateInventory();
        }, null, 2L);
        hotbarMenuService.syncSoon(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(hotbarMenuService::isToken);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        hotbarMenuService.ensureInstalled(event.getPlayer());
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        var held = event.getItem() != null ? event.getItem() : event.getPlayer().getInventory().getItemInMainHand();
        if (!hotbarMenuService.isToken(held)) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.open(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        hotbarMenuService.ensureInstalled(event.getPlayer());
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        if (!hotbarMenuService.isToken(event.getPlayer().getInventory().getItemInMainHand())) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.open(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        hotbarMenuService.ensureInstalled(event.getPlayer());
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        if (!hotbarMenuService.isToken(event.getPlayer().getInventory().getItemInMainHand())) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.open(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!hotbarMenuService.isToken(event.getItemDrop().getItemStack())) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.install(player);
        player.updateInventory();
        hotbarMenuService.syncSoon(player);
        hotbarMenuService.open(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        hotbarMenuService.ensureInstalled(player);
        int playerHotbarRawSlot = event.getView().getTopInventory().getSize() + HotbarMenuService.HOTBAR_SLOT;
        boolean reservedSlotClick = event.getRawSlot() == playerHotbarRawSlot;
        boolean reservedSlotNumberKey = event.getClick() == ClickType.NUMBER_KEY
                && event.getHotbarButton() == HotbarMenuService.HOTBAR_SLOT;
        boolean reservedSlotHasToken = hotbarMenuService.isToken(player.getInventory().getItem(HotbarMenuService.HOTBAR_SLOT));
        boolean clickedToken = hotbarMenuService.isToken(event.getCurrentItem());
        boolean cursorToken = hotbarMenuService.isToken(event.getCursor());
        boolean numberKeyUsesToken = event.getClick() == ClickType.NUMBER_KEY
                && event.getHotbarButton() >= 0
                && hotbarMenuService.isToken(player.getInventory().getItem(event.getHotbarButton()));
        boolean tokenInteraction = clickedToken || cursorToken || numberKeyUsesToken
                || (reservedSlotClick && reservedSlotHasToken)
                || (reservedSlotNumberKey && (clickedToken || reservedSlotHasToken));
        if (!tokenInteraction && !reservedSlotClick && !reservedSlotNumberKey) {
            return;
        }
        event.setCancelled(true);
        if (!tokenInteraction) {
            hotbarMenuService.syncSoon(player);
            return;
        }
        hotbarMenuService.openFromInventoryInteraction(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        int playerHotbarRawSlot = event.getView().getTopInventory().getSize() + HotbarMenuService.HOTBAR_SLOT;
        if (!event.getRawSlots().contains(playerHotbarRawSlot)) {
            return;
        }
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
            hotbarMenuService.ensureInstalled(player);
            hotbarMenuService.syncSoon(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (!hotbarMenuService.isToken(event.getMainHandItem()) && !hotbarMenuService.isToken(event.getOffHandItem())) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.ensureInstalled(event.getPlayer());
        hotbarMenuService.syncSoon(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (hotbarMenuService.isToken(event.getItem().getItemStack())) {
            event.setCancelled(true);
            event.getItem().remove();
        }
        hotbarMenuService.syncSoon(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!MenuTitles.isPluginMenu(event.getView().getTitle())) {
            return;
        }
        hotbarMenuService.resyncAfterMenuClose(player);
    }
}
