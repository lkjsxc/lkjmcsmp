# Scripted Check Contract

## Scope

Extended smoke suite:

1. Happy-path check per major system.
2. Permission-denial check per major system.
3. Cooldown/error-path check per major system where applicable.

## Major Systems

- Teleport (`tp`, `tpa`, `rtp`)
- Homes and warps
- Party operations
- Points conversion and shop purchase
- Pseudo-advancement claim flow
- GUI root menu open

## Minimum Assertions

- Command responds without server error.
- Expected success or failure message contains contract key phrase.
- Side effects are persisted when expected.
- `/tp` plain command path resolves to plugin behavior (or explicit diagnostic fallback).
- RTP path does not emit thread-context failure errors in server logs.
- First-join RTP marker state persists once per UUID.
