# lkjmcsmp Documentation Canon

## Goal

Define the canonical behavior and maintenance contracts for the plugin in an LLM-first format.

## Rules

1. Docs are authoritative; implementation follows docs.
2. Update docs contracts before code in every behavior-changing batch.
3. Keep one `README.md` per docs directory as the local table of contents.
4. Keep docs files at `<= 300` lines and authored source files at `<= 200` lines.
5. Prefer stable headings, terse bullets, and one canonical definition per concept.
6. Update parent `README.md` files in the same batch as child doc changes.

## Top-Level Index

- [getting-started/README.md](getting-started/README.md): bootstrap and verification entry
- [vision/README.md](vision/README.md): project purpose and LLM authoring constraints
- [product/README.md](product/README.md): player-facing systems and UX contracts
- [architecture/README.md](architecture/README.md): module, data, and Folia execution contracts
- [operations/README.md](operations/README.md): deployment and acceptance verification runbooks
- [repository/README.md](repository/README.md): repository structure and workflow rules

## Recommended Reading Order

1. [vision/purpose.md](vision/purpose.md)
2. [vision/llm-authoring.md](vision/llm-authoring.md)
3. [repository/layout/root-layout.md](repository/layout/root-layout.md)
4. [repository/workflow/change-sequence.md](repository/workflow/change-sequence.md)
5. [product/commands/teleport.md](product/commands/teleport.md)
6. [product/gui/hotbar-entrypoint.md](product/gui/hotbar-entrypoint.md)
7. [product/scoreboard/update-lifecycle.md](product/scoreboard/update-lifecycle.md)
8. [architecture/runtime/module-map.md](architecture/runtime/module-map.md)
9. [architecture/folia/scheduler-contract.md](architecture/folia/scheduler-contract.md)
10. [operations/verification/compose-pipeline.md](operations/verification/compose-pipeline.md)
