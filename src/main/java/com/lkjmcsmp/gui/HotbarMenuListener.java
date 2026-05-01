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
        hotbarMenuService.syncAfterRespawn(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(hotbarMenuService::isToken);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        hotbarMenuService.ensureInstalled(event.getPlayer());
        if (!hotbarMenuService.isEnabled(event.getPlayer())) {
            return;
        }
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getPlayer().getInventory().getHeldItemSlot() != HotbarMenuService.HOTBAR_SLOT) {
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
        if (!canOpenHeld(event.getPlayer(), event.getHand())) {
            return;
        }
        event.setCancelled(true);
        hotbarMenuService.open(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!canOpenHeld(event.getPlayer(), event.getHand())) {
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
        if (hotbarMenuService.isEnabled(player)
                && player.getInventory().getHeldItemSlot() == HotbarMenuService.HOTBAR_SLOT) {
            hotbarMenuService.open(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        hotbarMenuService.ensureInstalled(player);
        boolean reservedSlotClick = event.getClickedInventory() != null
                && event.getClickedInventory().equals(player.getInventory())
                && event.getSlot() == HotbarMenuService.HOTBAR_SLOT;
        boolean reservedSlotNumberKey = event.getClick() == ClickType.NUMBER_KEY
                && event.getHotbarButton() == HotbarMenuService.HOTBAR_SLOT;
        boolean reservedSlotHasToken = hotbarMenuService.isToken(player.getInventory().getItem(HotbarMenuService.HOTBAR_SLOT));
        boolean clickedToken = hotbarMenuService.isToken(event.getCurrentItem());
        boolean cursorToken = hotbarMenuService.isToken(event.getCursor());
        boolean numberKeyUsesToken = event.getClick() == ClickType.NUMBER_KEY
                && event.getHotbarButton() >= 0
                && hotbarMenuService.isToken(player.getInventory().getItem(event.getHotbarButton()));
        boolean staleTokenInteraction = clickedToken || cursorToken || numberKeyUsesToken;
        boolean opensReservedToken = reservedSlotHasToken && reservedSlotClick;
        if (!hotbarMenuService.isEnabled(player)) {
            if (staleTokenInteraction || reservedSlotHasToken) {
                event.setCancelled(true);
                hotbarMenuService.syncSoon(player);
            }
            return;
        }
        if (!opensReservedToken && !reservedSlotClick && !reservedSlotNumberKey && !staleTokenInteraction) {
            return;
        }
        event.setCancelled(true);
        if (!opensReservedToken) {
            hotbarMenuService.syncSoon(player);
            return;
        }
        hotbarMenuService.openFromInventoryInteraction(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        int topSize = event.getView().getTopInventory().getSize();
        boolean touchesReservedSlot = event.getRawSlots().stream()
                .anyMatch(rawSlot -> rawSlot >= topSize
                        && event.getView().convertSlot(rawSlot) == HotbarMenuService.HOTBAR_SLOT);
        if (!touchesReservedSlot) {
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

    private boolean canOpenHeld(Player player, org.bukkit.inventory.EquipmentSlot hand) {
        hotbarMenuService.ensureInstalled(player);
        return hotbarMenuService.isEnabled(player)
                && hand == org.bukkit.inventory.EquipmentSlot.HAND
                && player.getInventory().getHeldItemSlot() == HotbarMenuService.HOTBAR_SLOT
                && hotbarMenuService.isToken(player.getInventory().getItemInMainHand());
    }
}
