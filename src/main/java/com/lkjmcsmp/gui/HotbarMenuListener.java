package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class HotbarMenuListener implements Listener {
    private final HotbarMenuService hotbarMenuService;

    public HotbarMenuListener(HotbarMenuService hotbarMenuService) {
        this.hotbarMenuService = hotbarMenuService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        hotbarMenuService.install(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        hotbarMenuService.install(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!hotbarMenuService.isToken(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.open(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!hotbarMenuService.isToken(event.getItemDrop().getItemStack())) {
            return;
        }
        event.setCancelled(true);
        event.getItemDrop().remove();
        hotbarMenuService.open(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int playerHotbarRawSlot = event.getView().getTopInventory().getSize() + HotbarMenuService.HOTBAR_SLOT;
        boolean clickedHotbarSlot = event.getRawSlot() == playerHotbarRawSlot;
        boolean numberKeyOnHotbarSlot = event.getClick() == ClickType.NUMBER_KEY
                && event.getHotbarButton() == HotbarMenuService.HOTBAR_SLOT;
        if (!clickedHotbarSlot && !numberKeyOnHotbarSlot) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.open(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        int playerHotbarRawSlot = event.getView().getTopInventory().getSize() + HotbarMenuService.HOTBAR_SLOT;
        if (!event.getRawSlots().contains(playerHotbarRawSlot)) {
            return;
        }
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
            hotbarMenuService.install(player);
        }
    }
}
