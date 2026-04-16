# lkjmcsmp Documentation Canon

`docs/` is the only canonical source for product behavior, architecture, operations, and repository contracts.

## Global Rules

1. Update docs contracts before code.
2. Keep one `README.md` per docs directory as the local table of contents.
3. Keep docs files at `<= 300` lines.
4. Keep authored source files at `<= 200` lines.
5. Prefer declarative bullets and stable headings over long prose.
6. Keep canonical definitions singular; remove stale duplicates.
7. Optimize content retrieval for LLM-driven maintenance.

## Top-Level Sections

- [getting-started/README.md](getting-started/README.md): orientation, setup, and first verification path
- [vision/README.md](vision/README.md): project intent and LLM-first authoring rules
- [product/README.md](product/README.md): player-facing systems, commands, GUIs, and progression
- [architecture/README.md](architecture/README.md): runtime modules, data model, and Folia contracts
- [operations/README.md](operations/README.md): deployment and compose verification contracts
- [repository/README.md](repository/README.md): layout, line limits, and change workflow rules

## Recommended Reading Order

1. [vision/purpose.md](vision/purpose.md)
2. [repository/layout/root-layout.md](repository/layout/root-layout.md)
3. [product/commands/README.md](product/commands/README.md)
4. [product/gui/menu-tree.md](product/gui/menu-tree.md)
5. [product/economy/point-model.md](product/economy/point-model.md)
6. [product/advancements/progression-model.md](product/advancements/progression-model.md)
7. [product/scoreboard/sidebar-layout.md](product/scoreboard/sidebar-layout.md)
8. [architecture/runtime/module-map.md](architecture/runtime/module-map.md)
9. [architecture/data/sqlite-schema.md](architecture/data/sqlite-schema.md)
10. [architecture/folia/scheduler-contract.md](architecture/folia/scheduler-contract.md)
11. [operations/verification/compose-pipeline.md](operations/verification/compose-pipeline.md)
