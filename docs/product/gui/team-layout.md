# Team Menu Layout Contract

## Goal

Define a predictable and high-signal team menu layout that prioritizes common actions and isolates destructive actions.

## Canonical Slot Map

- Slot `10`: Team Info
- Slot `19`: Create Team
- Slot `20`: Invite Player
- Slot `21`: Accept Invite
- Slot `23`: Team Home
- Slot `24`: Set Team Home
- Slot `25`: Leave Team
- Slot `31`: Disband Team
- Slot `49`: Back (`ARROW`)

## Grouping Rules

1. Informational state appears first (`Team Info`) before action rows.
2. Frequent actions (create, invite, accept, home) stay in the center-left cluster (`19..24`).
3. Exit/destructive actions (`Leave Team`, `Disband Team`) are visually separated from the primary cluster.
4. `Disband Team` remains isolated and never adjacent to `Create Team`.

## Role-Aware Rendering Rules

1. Non-leaders see `Disband Team` as locked with explicit reason text.
2. Players outside a team see in-team actions as locked with explicit join/create guidance.
3. Invite acceptance remains visible even when not in a team, with clear availability text.
4. Locked actions keep stable slots to preserve muscle memory.
