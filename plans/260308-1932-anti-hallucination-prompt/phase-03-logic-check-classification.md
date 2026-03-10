# Phase 03: Thêm Bước kiểm tra logic + Siết phân loại lá số
Status: ✅ Complete
Dependencies: Phase 02

## Objective
Thêm bước kiểm tra mâu thuẫn cuối cùng và siết chặt phần phân loại lá số.

## Implementation Steps

### Task 1: Thêm bước "TRƯỚC KHI KẾT LUẬN"
- [x] Chèn một section mới giữa `PHÂN LOẠI LÁ SỐ` và `FORMAT ĐẦU RA`:
```text
=====================================
TRƯỚC KHI KẾT LUẬN — KIỂM TRA MÂU THUẤN

Rà soát lại toàn bộ phân tích để đảm bảo KHÔNG có mâu thuẫn giữa:
• Đánh giá lực Mệnh (Bước 2) và kết luận tổng thể
• Đánh giá từng cung (phần B) và phân loại lá số (phần D)
• Nhận định ở cung Tài bạch và kết luận về khả năng giàu có

Nếu phát hiện mâu thuẫn → ưu tiên căn cứ tinh hệ, sửa lại kết luận cho nhất quán.
```

### Task 2: Siết chặt phân loại lá số
- [x] Sửa phần `PHÂN LOẠI LÁ SỐ`, thay:
```text
Chỉ được kết luận khi có căn cứ tinh hệ.
```
thành:
```text
Chỉ được phân loại khi có ít nhất 2–3 yếu tố tinh hệ hỗ trợ rõ ràng.
Phải liệt kê cụ thể các yếu tố đó.
Nếu không đủ → ghi: "Lá số trung bình, chưa đủ dấu hiệu đặc biệt".
```

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` — hàm `constructPrompt`

## Test Criteria
- [ ] Build thành công
- [ ] Prompt output chứa section "TRƯỚC KHI KẾT LUẬN"

---
Next Phase: phase-04-testing.md
