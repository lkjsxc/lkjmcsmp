# Configuration Management

## Config Owners

- `config.yml`: gameplay defaults, cooldowns, limits, menu toggles
- `shop.yml`: base exchange rates
- `milestones.yml`: pseudo-advancement definitions

## Rules

1. Base rates are immutable docs contract values unless seasonal override is applied through command.
2. Seasonal overrides persist in SQLite runtime tables.
3. Config reload command must validate schema before apply.
4. Invalid config reload leaves previous config active.
