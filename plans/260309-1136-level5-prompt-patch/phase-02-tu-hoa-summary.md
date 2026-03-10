# Phase 02: Tứ Hóa Summary Block (F)
Status: ⬜ Pending
Dependencies: Phase 01

## Objective
Tạo block tường minh "TÓM TẮT TỨ HÓA" trong prompt, aggregate dữ liệu đã có sẵn
thành 1 bảng tập trung để AI dễ nhìn.

**Không tính toán mới** — chỉ "nhặt" dữ liệu đã có sẵn trong các cung và gom lại.

## Implementation Steps

### Task 1: Viết hàm `buildTuHoaSummary()` trong GeminiClient.kt

```kotlin
private fun buildTuHoaSummary(cungList: List<CungInfo>, daiVanInfo: String): String {
    val sb = StringBuilder()
    
    // 1. Tứ Hóa Bản Mệnh
    sb.appendLine("TỨ HÓA BẢN MỆNH:")
    val bmSuffixes = listOf("(Hóa Lộc)", "(Hóa Quyền)", "(Hóa Khoa)", "(Hóa Kỵ)")
    for (suffix in bmSuffixes) {
        val cung = cungList.find { c -> c.phuTinh.contains(suffix) }
        if (cung != null) {
            val starName = // tìm chính tinh tương ứng
            sb.appendLine("  $suffix: $starName → Cung ${cung.name} (${cung.chucNang})")
        }
    }
    
    // 2. Tứ Hóa Đại Vận
    sb.appendLine("TỨ HÓA ĐẠI VẬN:")
    val dvSuffixes = listOf("(ĐV. Hóa Lộc)", "(ĐV. Hóa Quyền)", "(ĐV. Hóa Khoa)", "(ĐV. Hóa Kỵ)")
    for (suffix in dvSuffixes) {
        val cung = cungList.find { c -> c.phuTinh.contains(suffix) }
        if (cung != null) {
            sb.appendLine("  $suffix → Cung ${cung.name} (${cung.chucNang})")
        }
    }
    
    // 3. Tứ Hóa Lưu Niên
    sb.appendLine("TỨ HÓA LƯU NIÊN:")
    val lnSuffixes = listOf("(L.Hóa Lộc)", "(L.Hóa Quyền)", "(L.Hóa Khoa)", "(L.Hóa Kỵ)")
    for (suffix in lnSuffixes) {
        val cung = cungList.find { c -> c.phuTinh.contains(suffix) }
        if (cung != null) {
            sb.appendLine("  $suffix → Cung ${cung.name} (${cung.chucNang})")
        }
    }
    
    return sb.toString()
}
```

### Task 2: Inject vào constructPrompt()
**Vị trí:** Trong `lasoContent`, ngay sau block `0. METADATA LÁ SỐ`, trước `1. THÔNG TIN CƠ BẢN`

```kotlin
val tuHoaSummary = buildTuHoaSummary(cungList, info.daiVanInfo)

// Inject vào lasoContent
"""
...
- Phi Tinh Tứ Hóa...

TÓM TẮT TỨ HÓA (ĐỌC TRƯỚC KHI LUẬN):
$tuHoaSummary

1. THÔNG TIN CƠ BẢN:
...
"""
```

### Task 3: Xác minh output chính xác
- Chạy test `deepDump_FullPromptMetadata` → phải thấy block "TÓM TẮT TỨ HÓA"
- Manual check: đọc prompt output xem data có khớp với data rải trong các cung không

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` — thêm hàm + inject

## Test Criteria
- [ ] Block "TÓM TẮT TỨ HÓA" xuất hiện trong prompt
- [ ] Dữ liệu trong block khớp với dữ liệu rải trong 12 cung
- [ ] 9 test hiện tại vẫn PASS

---
Next Phase: phase-03-testing.md
