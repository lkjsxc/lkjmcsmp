package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.HomeSlotCatalog;
import com.lkjmcsmp.domain.ShopEffectExecutor;
import com.lkjmcsmp.domain.model.ShopEntry;
import org.bukkit.entity.Player;

import java.util.OptionalInt;
import java.util.function.Consumer;

final class HomeSlotEffectExecutor implements ShopEffectExecutor {
    private final HomeService homeService;

    HomeSlotEffectExecutor(HomeService homeService) {
        this.homeService = homeService;
    }

    @Override
    public void execute(
            Player player,
            ShopEntry entry,
            int deductedPoints,
            Consumer<ShopEffectExecutor.Result> callback) {
        callback.accept(purchaseSlot(player, entry));
    }

    private ShopEffectExecutor.Result purchaseSlot(Player player, ShopEntry entry) {
        OptionalInt expected = HomeSlotCatalog.expectedPurchasedSlots(entry.key());
        if (expected.isEmpty()) {
            return ShopEffectExecutor.Result.fail("Invalid home slot upgrade. Purchase refunded.");
        }
        try {
            OptionalInt newLimit = homeService.purchaseAdditionalSlot(player.getUniqueId(), expected.getAsInt());
            if (newLimit.isEmpty()) {
                return ShopEffectExecutor.Result.fail("Buy home slot upgrades in order. Purchase refunded.");
            }
            return ShopEffectExecutor.Result.ok("Home slot unlocked. Home limit is now " + newLimit.getAsInt() + ".");
        } catch (Exception ex) {
            return ShopEffectExecutor.Result.fail("Home slot purchase failed: " + ex.getMessage());
        }
    }
}
