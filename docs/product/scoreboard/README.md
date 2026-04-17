# Scoreboard Contracts

## Goal

Define a deterministic, recoverable sidebar system that remains visible for every online player on Folia.

## Core Rules

1. Sidebar runtime uses Bukkit/Paper scoreboard APIs only (`Scoreboard`, `Objective`, `Team`, `DisplaySlot`).
2. External sidebar libraries and Packet/NMS sidebar backends are disallowed.
3. Join, startup, targeted refresh, retry, and periodic reconcile all use one shared render pipeline.
4. Equal snapshots must render equal title text, visible lines, ordering, and slot ownership.
5. Missing/corrupt objective state is recoverable through cleanup + full rebuild with bounded retries.
6. Data lookup failures degrade to explicit fallback values (`points=0`) and still render.
7. Sidebar ownership is explicit: reconcile always reasserts managed objective into `DisplaySlot.SIDEBAR`.
8. Reconcile and retry orchestration are player-scoped (no global repeating tick dependency).
9. Online count used in snapshots comes from tracked join/quit state, never cross-player scans in mutation tasks.
10. Sustained blank/missing sidebar for an online player after retry + reconcile window is a blocker regression.

## Assumptions

- Deterministic means equal input snapshots produce equal title text, visible lines, and visible ordering across all update paths.

## Child Index

- [render-architecture.md](render-architecture.md): component model and deterministic render pipeline
- [sidebar-layout.md](sidebar-layout.md): canonical title, visible lines, and stable entry identity rules
- [update-lifecycle.md](update-lifecycle.md): join/startup/targeted/retry/periodic/quit lifecycle
- [failure-model.md](failure-model.md): failure classes, retry policy, degrade/recovery behavior
