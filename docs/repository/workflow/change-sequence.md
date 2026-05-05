# Change Sequence

## Contract

1. Update docs contracts first (including affected parent `README.md` files).
2. Update code and config to match docs.
3. Update verification scripts/contracts if needed.
4. Run compose verification pipeline.
5. Commit coherent verified batch.
6. If Docker-created ignored artifacts break host Gradle, prefer compose verification over changing tracked files.

## Notes

- Keep each batch to one clear purpose.
- Do not mix unrelated refactors into feature batches.
