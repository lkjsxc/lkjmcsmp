# Scoreboard Contracts

## Goal

Define scoreboard content and lifecycle so sidebar rendering is reliable for all online players.

## Rules

1. Sidebar content and ordering remain deterministic.
2. Join and periodic refresh paths must both render scoreboard consistently.
3. Data lookup failures must degrade to explicit fallback values instead of hiding the sidebar.

## Child Index

- [sidebar-layout.md](sidebar-layout.md): canonical scoreboard title and lines
- [update-lifecycle.md](update-lifecycle.md): render, refresh, and teardown lifecycle
