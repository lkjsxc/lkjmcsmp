# Party Command Contract

## Goal

Provide lightweight social grouping with low operational overhead.

## Commands

- `/team create <name>`
- `/team invite <player>`
- `/team accept <name>`
- `/team kick <player>`
- `/team leave`
- `/team chat <message>`
- `/team home`
- `/team sethome`
- `/team disband`
- `/team info`

## Role Model

- `leader`
- `member`

## Rules

1. Each player belongs to at most one party.
2. Invite expires after configured timeout.
3. Only leader can kick, disband, and set party home.
4. Party chat is isolated to online party members.
5. Party home is optional and can be unset.
6. Team command namespace remains `/team` for user familiarity.
7. GUI disband flow requires explicit confirmation before `/team disband` is dispatched.

## Failures

- Invite target already in party: reject.
- Accept after timeout: reject.
- Unauthorized management action: reject with required role.
