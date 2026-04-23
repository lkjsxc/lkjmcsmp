package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import org.bukkit.entity.Player;

public interface ShopEffectExecutor {
    void execute(Player player, ShopEntry entry);
}
