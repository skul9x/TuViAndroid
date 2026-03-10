# Phase 04: Testing & Verification
Status: ✅ Complete
Dependencies: Phase 03

## Objective
Đảm bảo tất cả thay đổi hoạt động đúng: tests cũ không bị break, features mới được verify, APK build thành công.

## Files to Modify
- `app/src/test/java/com/example/tviai/core/DataLayerVerificationTest.kt`

## Implementation Steps

### A. Verify Existing Tests
1. [x] Chạy 11 tests hiện tại → PHẢI pass hết
   ```
   ./gradlew test --tests "com.example.tviai.core.DataLayerVerificationTest"
   ```

### B. Add New Tests
2. [x] `testLayer7_CanChi12Cung`:
   - 12 cung PHẢI có `canChi` không rỗng
   - Format phải là "Can Chi" (2 từ)
   - Spot check: Cung Dần cho Nhâm Thân 1992 = "Nhâm Dần" (startCan=8=Nhâm cho Đinh/Nhâm)

3. [x] `testLayer8_AmDuongMenh`:
   - Input Nam + Nhâm(Dương) → "Dương Nam"
   - Input Nữ + Nhâm(Dương) → "Dương Nữ"

4. [x] `testLayer9_PromptContainsLevel5Sections`:
   - Prompt phải chứa: "TỨ HÓA BẢN MỆNH", "NGŨ HÀNH", "GIỚI TÍNH", "TUẦN", "TRIỆT", "PHI TINH", "VẬN HẠN ĐA TẦNG", "KIỂM CHỨNG CHÉO"
   - Prompt phải chứa ít nhất 1 dòng Can Chi cung
   - Prompt phải chứa "Phi Tinh" data dòng

### C. Build & Final Verification
5. [x] Build APK thành công:
   ```
   ./gradlew assembleDebug
   ```
6. [x] Cập nhật `CHANGELOG.md` → v4.0 (Level 5 Upgrade)

## Test Criteria
- [x] 11 tests cũ: PASS
- [x] 3 tests mới: PASS
- [x] Total: 14/14 PASS
- [x] APK Build: SUCCESS

## Notes
- Test Phi Tinh output: Kiểm tra string format, không kiểm tra tính đúng về mặt Tử Vi (vì logic pre-compute dựa trên TU_HOA_MAP đã verified)
- Nếu test fail → quay lại Phase tương ứng để fix

---
End of Plan. Sau verify → `/save-brain` để lưu context.
