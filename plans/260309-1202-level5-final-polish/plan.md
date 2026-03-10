# Plan: Level 5 Final Polish (Expert Review Round 2)
Created: 2026-03-09 12:02
Status: ✅ Complete

## Overview
Vá 3 lỗ hổng cuối cùng trong prompt Level 5 theo nhận xét của chuyên gia lần 2.
**Không thay đổi logic tính toán** — chỉ thêm/sửa text trong prompt.

Chuyên gia đánh giá prompt hiện tại đạt **Level 4.8–5.0**, cần vá thêm 3 chỗ để ổn định hoàn toàn.

## Scope

| # | Hạng mục | File | Loại thay đổi | Mức độ |
|---|----------|------|---------------|--------|
| 1 | BƯỚC 4 – Kiểm tra mâu thuẫn sau 12 cung | GeminiClient.kt | Prompt text | ⚠️ Medium |
| 2 | Khóa vai trò Bảng tra Tứ Hóa 10 Can | GeminiClient.kt | Prompt text | ⚠️ Medium |
| 3 | Ép format vận năm bắt buộc (E1) | GeminiClient.kt | Prompt text (dynamic) | ⚠️ Medium |

## Phân tích chi tiết từ chuyên gia

### Gợi ý 1: BƯỚC 4 – Kiểm tra mâu thuẫn
- **Hiện trạng:** Có PHẦN 7 "KIỂM CHỨNG CHÉO" (L465-468) nhưng chỉ là hướng dẫn phương pháp, không ép AI thực hiện bước riêng biệt
- **Vấn đề:** AI có thể bỏ qua cross-check vì nó nằm xen giữa methodology, không phải quy trình bắt buộc
- **Giải pháp:** Thêm BƯỚC 4 vào "QUY TRÌNH PHÂN TÍCH BẮT BUỘC" (sau Bước 3b, L435)

### Gợi ý 2: Khóa Bảng tra Tứ Hóa
- **Hiện trạng:** Bảng tra nằm ở L274 với title "(DÙNG CHO PHI TINH)" — mơ hồ
- **Vấn đề:** AI có thể dùng bảng này để tự tính thêm Tứ Hóa cho các can chưa pre-compute
- **Giải pháp:** Thêm 1 câu ⚠️ ngay dưới bảng tra

### Gợi ý 3: Ép format vận năm
- **Hiện trạng:** Mục E (L605-611) chỉ có bullet "lưu ý vận hạn" — quá chung
- **Vấn đề:** AI thường viết sơ sài phần vận năm, đặc biệt năm đang xem
- **Giải pháp:** Thêm tiểu mục E1 bắt buộc với 5 phần phân tích cụ thể, dùng `${info.viewingYear}` động

## Phases

| Phase | Name | Status | Tasks | Progress |
|-------|------|--------|-------|----------|
| 01 | Prompt Text Patches (3 blocks) | ✅ Complete | 3 | 100% |
| 02 | Testing & Verify | ✅ Complete | 4 | 100% |

**Tổng:** 7 tasks | Ước tính: 1 session ngắn (~15 phút)

## Quick Commands
- Start: `/code phase-01`
- Check progress: `/next`
