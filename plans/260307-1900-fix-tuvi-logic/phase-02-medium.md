# Phase 02: Medium Bugs
Status: ⬜ Pending
Dependencies: Phase 01

## Tasks

### 1. Fix L.Văn Khúc incomplete (Bug 4)
- [ ] Add VAN_KHUC_MAP to Constants.kt (all 10 Can)
- [ ] Replace hardcoded `if (canNamXemIndex == 2)` with map lookup

### 2. Fix ĐV. Văn Khúc incomplete (Bug 5)
- [ ] Use same VAN_KHUC_MAP for Đại Vận Can lookup
- [ ] Replace hardcoded `if (canDaiVan == 9)` with map lookup

### 3. Fix calculateScores after brightness (Bug 6)
- [ ] Option A: Move calculateScores BEFORE anDoSang
- [ ] Option B: Strip brightness suffix before lookup
- [ ] Choose Option A (simpler, no side effects)

## Files
- `Constants.kt` — Add VAN_KHUC_MAP
- `TuViLogic.kt` — Lines 808-812, 921-927, 120-125

---
Next Phase: phase-03-minor.md
