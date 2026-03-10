# Phase 02: Prompt Cách Cục Ranking
Status: ✅ Complete
Dependencies: Phase 01

## Objective
Thêm block hướng dẫn AI xếp hạng và xử lý mâu thuẫn khi có nhiều cách cục trong 1 lá số.

## File to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt`

## Implementation Steps

### A. Thêm block "XẾP HẠNG CÁCH CỤC" vào Prompt

Thêm **sau BƯỚC 3 – KIỂM TRA CÁCH CỤC** (GeminiClient.kt ~line 400):

1. [x] Thêm block sau vào prompt
2. [x] Sửa metadata label "Bộ sao đã hình thành" → "Nhóm sao hội hợp"

### B. Cập nhật danh sách CÁCH CỤC trong prompt

3. [x] Trong BƯỚC 3 (line 385-400), thêm chú thích AI
4. [x] Bổ sung vào QUY ƯỚC KÝ HIỆU (line 578)
   ```
   • "Tam hợp [Bộ sao]" = 3 sao phân bố trên 3 cung tam hợp
   • "Nhóm [Bộ sao]" = các sao có mặt nhưng CHƯA xác nhận là cách cục
   ```

## Notes
- Block này giải quyết Lỗi 9 của reviewer: AI không biết cách xử lý khi có nhiều bộ sao.
- Sau khi thêm, AI sẽ tự so sánh lực giữa các cách → output chính xác hơn đáng kể.

---
Next Phase: phase-03 (Testing & Verify)
