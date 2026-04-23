# Shop Action Contract

## Goal

Define canonical behavior for every click action inside the Points Shop list, Points Shop detail, and cobblestone conversion surfaces.

## Shop List Actions (`lkjmcsmp :: shop`)

1. **Convert Cobblestone** (slot `45`): consumes all cobblestone in inventory and converts to Maruishi Points.
2. **Page Prev** (slot `46`): decrement page index, clamp at `0`, reopen shop list.
3. **Page Next** (slot `47`): increment page index, clamp at max, reopen shop list.
4. **Back** (slot `49`): return to root menu.
5. **Item entry click**: capture selected item key, open shop detail for that item.
6. **Item entry appearance**:
   - Physical items: show material, display name, price, and `"Selectable quantity: 1..64"` lore.
   - Service items: show configured material, display name, price, `"§dService Item — executes on purchase"` lore.

## Shop Detail Actions (`lkjmcsmp :: shop-detail`)

### Physical Items

1. **Buy x1, x2, x4, x8, x16, x32, x64** (slots `19..25`): deduct Maruishi Points and grant the item stack immediately.
2. **Back** (slot `49`): return to shop list at the previously viewed page.
3. Info panel (slot `4`) shows selected item summary.
4. Points balance indicator (slot `31`) shows current Maruishi Points.
5. After successful purchase, detail view refreshes affordability state and stays open.
6. After failed purchase, detail view stays open and player receives exact reason.

### Service Items

1. **Purchase** (slot `22`): deduct Maruishi Points and execute the registered service effect.
2. **Back** (slot `49`): return to shop list at the previously viewed page.
3. Info panel (slot `4`) shows selected service summary.
4. Points balance indicator (slot `31`) shows current Maruishi Points.
5. After successful purchase, return to shop list.
6. After failed purchase, return to shop list with exact reason.

## Cobblestone Conversion Rules

1. Conversion is triggered only by the **Convert Cobblestone** button in the shop list.
2. The button click is identified by display name alone; cursor contents do not affect eligibility.
3. The amount converted equals the total cobblestone count in the player's inventory.
4. If no cobblestone is present, the action fails with an explicit message.
5. On success, the shop list refreshes and the player sees the new Maruishi Points balance.
6. Conversion triggers achievement progress for `convert_amount`.

## Service Effect Execution Rules

1. Every service item has a registered `ShopEffectExecutor` keyed by its shop item key.
2. The executor receives the `ShopEntry` so it can read configuration such as `environment`.
3. The executor runs only after Maruishi Points deduction succeeds and is ledgered.
4. If the executor throws or returns failure, the Maruishi Points deduction is NOT rolled back automatically; the executor must handle its own compensating transactions.
5. No inventory capacity check is performed for service items.
6. The `temporary_dimension_pass` executor calls `TemporaryDimensionManager.createInstance` with the entry's configured environment.

## Failure Semantics

1. **Insufficient Maruishi Points**: action is rejected before deduction; no side effects run.
2. **Unknown shop item**: rejected with explicit "unknown shop item" message.
3. **Inventory full (physical items only)**: rejected before deduction.
4. **Effect execution failure**: Maruishi Points remain deducted; player receives explicit failure reason; audit log captures the failure.

## Cross-References

- [slot-maps.md](slot-maps.md): canonical slot positions for shop menus
- [../temporary-dimension/shop-integration.md](../temporary-dimension/shop-integration.md): dimension pass placement
