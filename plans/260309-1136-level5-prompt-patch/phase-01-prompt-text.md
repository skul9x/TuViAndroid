# Phase 01: Prompt Text Patches (A–E)
Status: ⬜ Pending
Dependencies: None

## Objective
Thêm 5 block text mới vào prompt trong `GeminiClient.kt` hàm `constructPrompt()`.
**Không sửa logic tính toán, chỉ thêm chữ.**

## Implementation Steps

### Task 1: Quy tắc ưu tiên khi mâu thuẫn
**Vị trí:** Sau block "NGUYÊN TẮC TUYỆT ĐỐI" (sau line ~355), trước "QUY TRÌNH PHÂN TÍCH"

```
QUY TẮC ƯU TIÊN KHI TÍN HIỆU MÂU THUẪN:
1. Chính tinh > Phụ tinh (chính tinh quyết định bản chất cung)
2. Miếu/Vượng > Đắc > Bình > Hãm (sáng quyết định lực)
3. Tứ hóa bản mệnh > Tứ hóa đại vận > Tứ hóa lưu niên
4. Đồng cung > Tam hợp > Xung chiếu > Giáp cung
5. Cách cục lớn > Tiểu cách (cách lớn chi phối toàn cục)
```

### Task 2: Trọng số tương tác
**Vị trí:** Trong block "PHƯƠNG PHÁP LUẬN MỖI CUNG" (sau Bước 3, trước Bước 4)

```
QUY TẮC TRỌNG SỐ TƯƠNG TÁC:
- Đồng cung: 100% lực (mạnh nhất)
- Tam hợp hội chiếu: 70-80% lực
- Xung chiếu (đối cung): 60-70% lực (ảnh hưởng gián tiếp)
- Giáp cung: 40-50% lực (hỗ trợ/kìm hãm từ hai bên)
- Nhị hợp: 30% lực (yếu nhất)
⚠️ Sát tinh xung chiếu gây hại ÍT HƠN sát tinh đồng cung.
⚠️ Cát tinh tam hợp hội chiếu có lực MẠNH HƠN cát tinh giáp cung.
```

### Task 3: Mở rộng quy tắc Vô chính diệu
**Vị trí:** Thay thế dòng đơn giản hiện tại ở QUY ƯỚC KÝ HIỆU (line ~593)

Thay:
```
• Cung không có chính tinh = Vô chính diệu → xem chính tinh cung đối chiếu (xung chiếu) để luận
```

Bằng:
```
• QUY TẮC VÔ CHÍNH DIỆU (4 bước):
  Bước 1: Mượn chính tinh cung đối chiếu (xung chiếu) — giảm 30% lực so với sao ở bản cung.
  Bước 2: Phụ tinh trong cung vô chính diệu trở thành "chủ thực tế" — phân tích kỹ hơn.
  Bước 3: Vô chính diệu + nhiều sát tinh → cung rất yếu, biến động lớn.
  Bước 4: Vô chính diệu + nhiều cát tinh → "đất trống gặp mưa" — muộn phát nhưng có thể phát.
```

### Task 4: Negative Examples
**Vị trí:** Sau block "QUY ƯỚC KÝ HIỆU", trước "Nội dung lá số:"

```
CÁC LỖI PHỔ BIẾN AI KHÔNG ĐƯỢC MẮC:
❌ SAI: "Tử Vi là sao vua nên ở đâu cũng tốt" → PHẢI xét miếu/hãm, cung vị.
❌ SAI: "Kình Dương luôn xấu" → Kình Dương miếu (VD: Ngọ) có thể tạo Mã Đầu Đới Kiếm.
❌ SAI: "Hóa Kỵ luôn xấu" → Kỵ ở Quan/Tài có thể chỉ là "chuyên tâm, bám víu".
❌ SAI: Luận Vô Chính Diệu mà không nhắc chính tinh đối cung.
❌ SAI: Gộp cát tinh + sát tinh → "trung bình" → PHẢI phân tích cơ chế: cát giảm sát hay sát phá cát.
```

### Task 5: Chấm điểm LỰC CUNG + ĐỘ TIN CẬY
**Vị trí:** Block "FORMAT KẾT LUẬN" (line ~521), bổ sung vào cuối

Thêm sau "→ biểu hiện thực tế.":
```
→ 🔹 LỰC CUNG: [1-10] (1=rất yếu, 10=rất mạnh)
→ 🔹 XU HƯỚNG: [Thuận/Nghịch/Biến động]

Mỗi kết luận lớn (cách cục, vận hạn) phải kèm:
📊 ĐỘ TIN CẬY: [Cao/Trung bình/Thấp]
- Cao: ≥3 căn cứ tinh hệ khớp nhau, không mâu thuẫn.
- Trung bình: 1-2 căn cứ, hoặc có mâu thuẫn nhẹ.
- Thấp: Thiếu dữ liệu hoặc nhiều mâu thuẫn.
```

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` — hàm `constructPrompt()`

## Test Criteria
- [ ] Prompt output chứa tất cả 5 block mới
- [ ] Build thành công (không syntax error)
- [ ] 9 test hiện tại vẫn PASS

---
Next Phase: phase-02-tu-hoa-summary.md
