package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.persistence.HomeSlotPurchaseDao;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class HomeSlotPurchaseService {
    private final HomeSlotPurchaseDao purchases;
    private final HomeService homeService;

    public HomeSlotPurchaseService(HomeSlotPurchaseDao purchases, HomeService homeService) {
        this.purchases = purchases;
        this.homeService = homeService;
    }

    public Optional<ShopEntry> nextUpgrade(Player player) throws Exception {
        return HomeSlotCatalog.nextEntry(homeService.purchasedHomeSlots(player.getUniqueId()));
    }

    public Result purchaseNext(Player player) throws Exception {
        var playerId = player.getUniqueId();
        int purchased = homeService.purchasedHomeSlots(playerId);
        Optional<ShopEntry> next = HomeSlotCatalog.nextEntry(purchased);
        if (next.isEmpty()) {
            return Result.fail("home slot limit is already maxed");
        }
        var result = purchases.purchaseNext(playerId, purchased);
        return switch (result.status()) {
            case PURCHASED -> Result.ok("purchased " + next.get().displayName()
                    + "; Home limit is now " + homeService.limitForPurchasedSlots(result.purchasedSlots()));
            case INSUFFICIENT_POINTS -> Result.fail("insufficient Points");
            case ORDER_CHANGED -> Result.fail("home slot purchase order changed; try again");
            case LIMIT_REACHED -> Result.fail("home slot limit is already maxed");
        };
    }

    public record Result(boolean success, String message) {
        public static Result ok(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
