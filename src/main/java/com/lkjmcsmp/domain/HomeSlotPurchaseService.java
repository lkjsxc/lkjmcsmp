package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.persistence.PointsDao;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class HomeSlotPurchaseService {
    private final PointsDao pointsDao;
    private final HomeService homeService;

    public HomeSlotPurchaseService(PointsDao pointsDao, HomeService homeService) {
        this.pointsDao = pointsDao;
        this.homeService = homeService;
    }

    public Optional<ShopEntry> nextUpgrade(Player player) throws Exception {
        return HomeSlotCatalog.nextEntry(homeService.purchasedHomeSlots(player.getUniqueId()));
    }

    public Result purchaseNext(Player player) throws Exception {
        int purchased = homeService.purchasedHomeSlots(player.getUniqueId());
        Optional<ShopEntry> next = HomeSlotCatalog.nextEntry(purchased);
        if (next.isEmpty()) {
            return Result.fail("home slot limit is already maxed");
        }
        ShopEntry entry = next.get();
        int balance = pointsDao.getBalance(player.getUniqueId());
        if (balance < entry.points()) {
            return Result.fail("insufficient Points");
        }
        pointsDao.addPoints(player.getUniqueId(), -entry.points(), "HOME_SLOT_PURCHASE",
                "{\"slot\":\"" + entry.key() + "\"}");
        try {
            var newLimit = homeService.purchaseAdditionalSlot(player.getUniqueId(), purchased);
            if (newLimit.isPresent()) {
                return Result.ok("purchased " + entry.displayName()
                        + "; Home limit is now " + newLimit.getAsInt());
            }
            refund(player, entry);
            return Result.fail("home slot purchase order changed; try again");
        } catch (Exception ex) {
            refund(player, entry);
            throw ex;
        }
    }

    private void refund(Player player, ShopEntry entry) throws Exception {
        pointsDao.addPoints(player.getUniqueId(), entry.points(), "HOME_SLOT_PURCHASE_REFUND",
                "{\"slot\":\"" + entry.key() + "\"}");
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
