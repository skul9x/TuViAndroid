# Phase 04: Testing & Verification
Status: ⬜ Pending
Dependencies: Phase 01-03

## Tasks

### 1. Update existing test case
- [ ] Fix expected values in testNguyenDuyTruong (will change after Bug 1/2 fix)

### 2. Add new test cases
- [ ] Test anCungMenhThan direction (nghịch verified)
- [ ] Test Thiên Phủ position for 3 different Tử Vi positions
- [ ] Test calculateScores returns > 0 for cung with chính tinh

### 3. Cross-reference with known lá số
- [ ] Compare output with tuvi.cohoc.vn for sample inputs

### 4. Run full build
- [ ] `./gradlew testDebugUnitTest`
- [ ] `./gradlew assembleDebug`
