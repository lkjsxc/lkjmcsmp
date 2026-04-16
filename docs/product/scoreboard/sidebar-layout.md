# Sidebar Layout Contract

## Goal

Provide a clean, typical-SMP sidebar with essential status only.

## Title

- `lkjmcsmp SMP`

## Required Lines

1. `Online: <count>`
2. `Points: <balance>`

## Rules

1. Line ordering is stable.
2. Missing data uses explicit fallback values (`0` for points).
3. Sidebar format remains compact and readable without color dependency.
