# Temporary Dimension Shop Integration

## Summary

The dimension pass is a service-type shop item placed among natural blocks for visual impact.

## Placement

1. In the Points Shop list, the dimension pass appears among logs and terrain blocks.
2. This creates the feeling of discovering a mysterious egg among ordinary materials.

## Visual Design

1. Material: `DRAGON_EGG`
2. Display name: `§5§kM§r §5Mysterious Egg §5§kM` (obfuscated edges for mystery)
3. Lore: `"§dService Item — executes on purchase"`
4. Price: `10,000` Cobblestone Points

## Behavior

1. Clicking the item opens the shop detail page.
2. Detail page shows a single **Purchase** button (no quantity selection for service items).
3. On purchase, Cobblestone Points are deducted and a temporary dimension instance is created immediately.
4. The environment is read from the shop entry configuration (`THE_END` by default).

## Cross-References

- [purchase-and-creation.md](purchase-and-creation.md): creation logic after purchase
- [../gui/shop-actions.md](../gui/shop-actions.md): service item interaction contract
