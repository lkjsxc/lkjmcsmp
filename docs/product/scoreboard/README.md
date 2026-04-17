# Scoreboard Contracts

## Goal

Define a deterministic, recoverable sidebar contract so every online player keeps a visible scoreboard.

## Reliability Invariants

1. Sidebar rendering uses Bukkit/Paper scoreboard APIs only (`Scoreboard`, `Objective`, `Team`, `DisplaySlot`).
2. External sidebar libraries are not allowed, including fallback paths.
3. Join render, startup reconcile, and periodic reconcile must converge to the same layout contract.
4. Data lookup failures degrade to explicit fallback values instead of hiding the sidebar.
5. Missing/corrupt objective state is a recoverable fault: retry + rebuild rules must restore visibility deterministically.
6. Any sustained missing sidebar state for an online player is a regression.
7. Plugin owns sidebar slot for online players and reclaims `SIDEBAR` during reconcile.
8. Periodic reconcile is player-scoped and does not rely on a global tick source.
9. Online count used for rendering is tracked state, not cross-player scans inside player mutation tasks.

## Assumptions

- Deterministic behavior means equal input snapshots produce equal title text, line text, and line order across all update paths.

## Child Index

- [sidebar-layout.md](sidebar-layout.md): canonical scoreboard title and lines
- [update-lifecycle.md](update-lifecycle.md): Folia-safe render, retry, reconcile, and teardown lifecycle
