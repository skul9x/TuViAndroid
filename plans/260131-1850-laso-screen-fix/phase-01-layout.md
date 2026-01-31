# Phase 01: UI Layout Refactor
Status: ✅ Complete
Dependencies: None

## Objective
Refactor `LasoScreen.kt` to allow the central panel to occupy the full 2x2 center grid space (50% height/width) instead of being trapped in a single row.

## Requirements
### Functional
- [x] **Layout Structure:** Change to a Layered Box approach.
    - Layer 1: Outer Grid Cells (Rows 1-4, with holes in center of Row 2 & 3).
    - Layer 2: Central Info Panel centered in the Box, taking 50% width and height.
- [x] **Name Alignment:** Ensure the User Name is centered horizontally (`TextAlign.Center`).
- [x] **Copy Button:** Ensure the Copy Prompt button is visible and functional.

## Implementation Steps
1. [x] Modify `LasoScreen` composable to use `Box` layout strategy.
2. [x] Update `CentralInfo` to remove sizing constraints that might rely on parent column.
3. [x] Apply `TextAlign.Center` to the name text.

## Files to Create/Modify
- `app/src/main/java/com/example/tviai/ui/screens/LasoScreen.kt`

---
Next Phase: [Phase 02](phase-02-verification.md)
