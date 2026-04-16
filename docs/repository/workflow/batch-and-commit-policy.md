# Batch and Commit Policy

## Frequency

- Commit after each coherent verified batch.
- Land docs-only batches before dependent code batches.

## Commit Message Rules

- Summarize changed contract scope.
- Mention key behavior changes.
- Include verification context in body when non-trivial.

## Batch Examples

- docs IA + contracts only
- economy contract + economy service implementation
- GUI contract + menu implementation
