package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface ShopEffectExecutor {
    void execute(Player player, ShopEntry entry, int deductedPoints, Consumer<Result> callback);

    record Result(boolean success, String message) {
        public static Result ok(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
