# Tip Delivery

## Goal

Send useful command-discovery tips without interrupting gameplay.

## Scheduling

1. Config path: `tips.enabled`.
2. Config path: `tips.interval-ticks`.
3. Default interval: `72000` ticks, approximately one hour.
4. Scheduling starts after plugin services are initialized.
5. Scheduling stops through the plugin scheduler cancellation path.
6. Each send pass iterates currently online players.
7. Offline players are skipped silently.

## Rotation

1. The next tip index is derived from player UUID plus a global send counter.
2. The same global send pass may show different tips to different players.
3. Empty catalogs suppress sends rather than emitting placeholders.
4. Tip text is prefixed consistently so players can identify it as guidance.

## Output

1. English prefix: `[Tip]`.
2. Japanese prefix: `[ヒント]`.
3. Messages should be one line.
4. Messages should prefer concrete command literals over abstract descriptions.
