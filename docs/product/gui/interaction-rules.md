# GUI Interaction Rules

## Input Rules

1. Left-click triggers primary action.
2. Right-click opens details where available.
3. Shift-click is ignored unless explicitly documented.
4. Closed inventories clear temporary session context.

## Feedback Rules

1. Success sends concise confirmation message only after action completion.
2. Failure sends exact reason and required action.
3. Cooldown and timeout show numeric remaining time.
4. Permission failures include missing node.

## Consistency Rules

1. GUI action handlers delegate to same services as command handlers.
2. Menu pagination preserves stable ordering.
3. Disabled actions render with explicit locked tooltip.
4. Every menu has a deterministic back path to root.
5. Every plugin inventory view uses 54 slots.
