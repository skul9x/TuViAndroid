# Phase 02: Testing & Verify
Status: ✅ Complete
Dependencies: Phase 01

## Objective
Xác nhận 3 block mới đã được inject đúng vào prompt + toàn bộ test suite PASS + build APK thành công.

## Implementation Steps

### Task 1: Thêm test verify 3 blocks mới
**File:** `app/src/test/java/com/example/tviai/core/Level5DeepVerificationTest.kt`

Thêm 1 test function mới:
```kotlin
@Test
fun `Level5 Final Polish - 3 blocks present`() {
    val prompt = client.getPromptForCopy(testLasoData)
    
    // Block 1: Bước 4 kiểm tra mâu thuẫn
    assertTrue("Missing BƯỚC 4 mâu thuẫn", 
        prompt.contains("BƯỚC 4") && prompt.contains("KIỂM TRA MÂU THUẪN"))
    assertTrue("Missing Mệnh vs Thân check",
        prompt.contains("Mệnh vs Thân"))
    assertTrue("Missing Mệnh vs Quan vs Tài check",
        prompt.contains("Mệnh vs Quan vs Tài"))
    
    // Block 2: Khóa bảng tra Tứ Hóa
    assertTrue("Missing Tứ Hóa table warning",
        prompt.contains("CHỈ dùng để GIẢI THÍCH cơ chế"))
    assertTrue("Missing anti-calculation lock",
        prompt.contains("KHÔNG dùng bảng này để tự an sao"))
    
    // Block 3: Format vận năm bắt buộc
    assertTrue("Missing E1 yearly transit section",
        prompt.contains("E1. Vận năm"))
    assertTrue("Missing mandatory yearly analysis steps",
        prompt.contains("Trùng điệp tứ hóa"))
}
```

### Task 2: Chạy full test suite
```bash
./gradlew test
```
**Expected:** Tất cả tests PASS (11 cũ + 1 mới = 12 tests)

### Task 3: Clean build APK
```bash
./gradlew clean assembleDebug
```
**Expected:** BUILD SUCCESSFUL, APK generated

### Task 4: Spot-check prompt output
- Copy prompt ra text file bằng `getPromptForCopy()`
- Verify thủ công 3 blocks ở đúng vị trí trong prompt

## Test Criteria
- [ ] 12/12 tests PASS
- [ ] Build thành công
- [ ] APK xuất ra không lỗi
- [ ] 3 blocks mới nằm đúng vị trí trong prompt output

---
Previous Phase: phase-01-prompt-text.md
