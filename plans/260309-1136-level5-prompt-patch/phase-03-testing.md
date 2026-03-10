# Phase 03: Testing & Verify
Status: ⬜ Pending
Dependencies: Phase 01, 02

## Objective
Xác minh tất cả patches không làm hỏng gì, prompt output đúng format.

## Implementation Steps

### Task 1: Thêm test mới `deepDump_Level5PatchBlocks`
File: `Level5DeepVerificationTest.kt`

Kiểm tra prompt chứa tất cả 6 block mới:
- "QUY TẮC ƯU TIÊN KHI TÍN HIỆU MÂU THUẪN"
- "QUY TẮC TRỌNG SỐ TƯƠNG TÁC"
- "QUY TẮC VÔ CHÍNH DIỆU (4 bước)"
- "CÁC LỖI PHỔ BIẾN AI KHÔNG ĐƯỢC MẮC"
- "LỰC CUNG:"
- "TÓM TẮT TỨ HÓA"

### Task 2: Thêm test mới `deepDump_TuHoaSummaryAccuracy`
File: `Level5DeepVerificationTest.kt`

Kiểm tra nội dung block Tóm tắt Tứ Hóa:
- Có đủ 4 dòng Bản Mệnh (Lộc + Quyền + Khoa + Kỵ)
- Có đủ 4 dòng Đại Vận
- Có đủ 4 dòng Lưu Niên
- Tên cung trong summary khớp với data trong 12 cung

### Task 3: Chạy toàn bộ test suite
```
.\gradlew :app:testDebugUnitTest --tests "com.example.tviai.core.*"
```

Kỳ vọng: 11/11 PASS (9 cũ + 2 mới)

### Task 4: Build APK kiểm tra
```
.\gradlew assembleDebug
```

Kỳ vọng: BUILD SUCCESSFUL

## Test Criteria
- [ ] 11/11 unit tests PASS
- [ ] APK build thành công
- [ ] Không có warning/error mới

---
✅ DONE → /save-brain
