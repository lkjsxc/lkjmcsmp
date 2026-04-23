# Team Menu Layout Contract

## Goal

Define a predictable and high-signal team menu layout that prioritizes common actions and isolates destructive actions.

## Canonical Slot Map

- Slot `10`: Team Info
- Slot `12`: Create Team
- Slot `13`: Accept Invite
- Slot `14`: Invite Player
- Slot `15`: Team Home
- Slot `16`: Set Team Home
- Slot `21`: Leave Team
- Slot `25`: Disband Team
- Slot `49`: Back (`ARROW`)

## Member List Display

1. Slots `28..34` display up to 7 member heads.
2. Each head shows the member's name as display name.
3. If there are more than 7 members, the first 6 heads are shown plus a `+N more` indicator at slot `34`.
4. Member heads use `PLAYER_HEAD` with `SkullMeta` set to the member's UUID so skins render correctly.
5. If the player is not in a team, the member list area remains empty.

## Grouping Rules

1. Informational state appears first (`Team Info`) before action rows.
2. Frequent actions (create, invite, accept, home) stay in the top-row cluster (`12..16`).
3. Exit/destructive actions (`Leave Team`, `Disband Team`) are visually separated in the second row (`21` and `25`).
4. `Disband Team` remains isolated and never adjacent to `Create Team`.
5. Clicking `Disband Team` opens a dedicated confirmation screen before command execution.

## Role-Aware Rendering Rules

1. Non-leaders see `Disband Team` as locked with explicit reason text.
2. Players outside a team see in-team actions as locked with explicit join/create guidance.
3. Invite acceptance remains visible even when not in a team, with clear availability text.
4. Locked actions keep stable slots to preserve muscle memory.

## Disband Confirmation Rules

1. Confirm view must display explicit destructive warning text.
2. `Confirm Disband` is separated from `Cancel` by at least one slot.
3. Cancel returns to Team menu without side effects.
4. After confirm success/failure, Team menu state refreshes immediately.

## Cross-References

- [slot-maps.md](slot-maps.md): shared navigation and border contracts
- [menu-tree.md](menu-tree.md): team menu placement in hierarchy
