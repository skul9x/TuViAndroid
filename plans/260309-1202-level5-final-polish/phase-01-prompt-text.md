# Phase 01: Prompt Text Patches (3 blocks)
Status: ✅ Complete
Dependencies: None

## Objective
Thêm/sửa 3 block text trong `GeminiClient.kt` hàm `constructPrompt()`.
**Không sửa logic tính toán, chỉ thêm/sửa chữ trong prompt.**

## Implementation Steps

### Task 1: BƯỚC 4 – Kiểm tra mâu thuẫn sau 12 cung
**Vị trí:** Sau BƯỚC 3b (line ~435), trước "7 PHƯƠNG PHÁP LUẬN BẮT BUỘC"

**Lý do:** Hiện QUY TRÌNH PHÂN TÍCH BẮT BUỘC chỉ có 3 bước (Tóm tắt → Đánh giá lực → Kiểm tra cách cục). Thiếu bước hậu kiểm sau khi đã luận 12 cung, dẫn đến AI có thể luận Mệnh một đằng, Quan/Tài một nẻo mà không rà soát.

**Cập nhật dòng 371:** Đổi "3 bước" → "4 bước":
```
Trước khi luận chi tiết phải thực hiện 4 bước.
```

**Thêm sau line 435** (sau block BƯỚC 3b):
```text
-------------------------------------
BƯỚC 4 – KIỂM TRA MÂU THUẪN (BẮT BUỘC SAU KHI LUẬN 12 CUNG)

Sau khi luận xong 12 cung, phải rà soát:
- Mệnh vs Thân: Bẩm sinh vs Hành động có khớp không?
- Mệnh vs Quan vs Tài: Tâm (Mệnh) - Tầm (Quan) - Lộc (Tài) logic với nhau không?
- Phu Thê vs Phúc Đức: Duyên nợ có khớp với phúc phần không?
- Tật Ách vs Mệnh: Sức khỏe có tương ứng với cường độ Mệnh không?

Nếu có mâu thuẫn, phải giải thích cơ chế ưu tiên theo "Quy tắc ưu tiên khi tín hiệu mâu thuẫn" ở trên. KHÔNG ĐƯỢC để hai kết luận song song mà không phân chủ-thứ.
```

---

### Task 2: Khóa vai trò Bảng tra Tứ Hóa 10 Can
**Vị trí:** Ngay sau `$canTuHoaTable` (line ~275), trước section "1. THÔNG TIN CƠ BẢN"

**Lý do:** Prompt đưa bảng tra 10 can nhưng rule 8 cấm AI tự tính — tạo mâu thuẫn ngầm. AI có thể lạm dụng bảng để tự suy ra Tứ Hóa chưa pre-compute. Cần 1 câu khóa chặt.

**Thêm sau line 275:**
```text
⚠️ Bảng tra Tứ Hóa 10 Can CHỈ dùng để GIẢI THÍCH cơ chế của dữ liệu Phi Tinh Tứ Hóa đã pre-compute.
KHÔNG dùng bảng này để tự an sao, tự tính thêm tứ hóa hoặc suy ra dữ liệu chưa được cung cấp.
```

---

### Task 3: Ép format vận năm bắt buộc (E1)
**Vị trí:** Thay block `E. KẾT LUẬN TỔNG THỂ` (line ~605-611)

**Lý do:** Hiện mục E chỉ liệt kê 5 bullet chung chung, đặc biệt "lưu ý vận hạn" quá mơ hồ. AI thường lướt qua phần vận năm. Cần ép format cụ thể.

**Thay dòng 605-611:**
```text
E. KẾT LUẬN TỔNG THỂ

• sức mạnh tổng thể lá số
• khả năng giàu có
• khả năng quyền lực
• hướng phát triển sự nghiệp

E1. Vận năm ${info.viewingYear} (BẮT BUỘC – KHÔNG ĐƯỢC BỎ QUA)
Phân tích theo thứ tự:
(1) Đại vận hiện tại → ảnh hưởng nền
(2) Lưu niên ${info.viewingYear} → sao lưu + lưu tứ hóa
(3) Trùng điệp tứ hóa → Song Lộc/Song Kỵ/Lộc Kỵ giao nhau
(4) Tác động lên Mệnh – Quan – Tài – Phu Thê
(5) Kết luận: thuận lợi / rủi ro chính trong năm
```

> **Lưu ý kỹ thuật:** `${info.viewingYear}` là biến Kotlin, sẽ được thay bằng năm thực tế (VD: 2026) khi build prompt. Không hardcode.

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` — hàm `constructPrompt()`

## Test Criteria
- [ ] Prompt output chứa "BƯỚC 4 – KIỂM TRA MÂU THUẪN"
- [ ] Prompt output chứa câu cảnh báo bảng tra Tứ Hóa
- [ ] Prompt output chứa "E1. Vận năm [YEAR]" với year = viewingYear
- [ ] Build thành công (không syntax error)
- [ ] Tất cả test hiện tại vẫn PASS

---
Next Phase: phase-02-testing.md
