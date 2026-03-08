# Phase 02: Củng cố Quy trình 3 bước (Bước 1-2-3)
Status: ✅ Complete
Dependencies: Phase 01

## Objective
Làm rõ và siết chặt 3 bước phân tích bắt buộc để AI không bỏ sót hoặc luận mơ hồ.

## Implementation Steps

### Task 1: Bước 1 — Xử lý cung Vô chính diệu
- [x] Tại `BƯỚC 1 – TÓM TẮT CẤU TRÚC LÁ SỐ`, thêm dòng:
```text
• Đối với cung vô chính diệu, bắt buộc ghi:
  "Cung [Tên]: Vô chính diệu → xung chiếu [Tên sao chính tinh cung đối diện]"
```

### Task 2: Bước 2 — Tiêu chí đánh giá lực Mệnh
- [x] Sửa `• Mệnh mạnh hay yếu` thành:
```text
• Đánh giá lực Mệnh dựa trên:
  - trạng thái miếu/vượng/đắc/hãm của chính tinh tại Mệnh
  - số lượng cát tinh nâng đỡ (Tả Hữu, Xương Khúc, Khôi Việt...)
  - số lượng sát tinh phá (Kình Đà, Hỏa Linh, Không Kiếp...)
  - ảnh hưởng Tuần / Triệt (nếu có)
```

### Task 3: Bước 3 — Điều kiện cách cục chặt chẽ
- [x] Tại `BƯỚC 3 – KIỂM TRA CÁCH CỤC`, thêm sau danh sách các cách:
```text
⛔ QUY TẮC KIỂM TRA CÁCH CỤC:
• Chỉ xác nhận cách khi các sao tạo cách xuất hiện đúng vị trí trong dữ liệu.
• Nếu không đủ điều kiện → ghi: "Không đủ dữ kiện để xác nhận cách".
• Không tự suy diễn cách cục theo trường phái khác.
```

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` — hàm `constructPrompt`, phần QUY TRÌNH PHÂN TÍCH BẮT BUỘC

## Test Criteria
- [ ] Build thành công
- [ ] Prompt output chứa đủ 3 bước cải tiến

---
Next Phase: phase-03-logic-check-classification.md
