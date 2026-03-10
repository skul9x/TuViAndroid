# Phase 01: Critical Bugs
Status: ⬜ Pending
Dependencies: None

## Objective
Fix 3 bugs that make the entire lá số incorrect.

## Tasks

### 1. Fix anCungMenhThan — Wrong direction (Bug 1)
- [ ] Line 180: `(menhPos + i)` → `(menhPos - i + 12) % 12`
- [ ] Remove dead code at line 172

### 2. Fix anChinhTinh — Thiên Phủ symmetry axis (Bug 2)
- [ ] Line 278: `(4 - tuViPos)` → `(10 - tuViPos)`
- [ ] Verify: Tử Vi at Dần(2) → Thiên Phủ should be at Thân(8). `(10-2)%12 = 8` ✓

### 3. Verify anCuc formula (Bug 3)
- [ ] Test against all known examples
- [ ] If wrong: replace with lookup table

## Files
- `TuViLogic.kt` lines 148-232, 234-298

---
Next Phase: phase-02-medium.md
