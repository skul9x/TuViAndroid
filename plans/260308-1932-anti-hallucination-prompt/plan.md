# Plan: Anti-Hallucination Prompt Upgrade
Created: 2026-03-08T19:32
Status: 🟡 In Progress

## Overview
Nâng cấp prompt AI trong `GeminiClient.kt` để loại bỏ hoàn toàn các kẽ hở gây hallucination. Dựa trên đánh giá chuyên gia (9/10 → mục tiêu 11/10).

## 6 Điểm yếu cần khắc phục
1. Chưa định nghĩa điều kiện cách cục → AI tự suy diễn
2. Chưa chuẩn hóa phân loại sao (cát/sát/phụ)
3. Thiếu bước kiểm tra mâu thuẫn logic trước khi kết luận
4. Tiêu chí đánh giá "Mệnh mạnh/yếu" mơ hồ
5. Phân loại lá số quá dễ dãi (thiếu ngưỡng tối thiểu)
6. Bước liệt kê chính tinh chưa xử lý rõ cung vô chính diệu

## Phases

| Phase | Name | Status | Tasks |
|-------|------|--------|-------|
| 01 | Siết chặt Nguyên tắc & Phân loại sao | ✅ Complete | 2 |
| 02 | Củng cố Quy trình 3 bước (Bước 1-2-3) | ✅ Complete | 3 |
| 03 | Thêm Bước kiểm tra logic + Siết phân loại lá số | ✅ Complete | 2 |
| 04 | Testing & Build APK | ✅ Complete | 3 |

**Tổng:** 10 tasks | Ước tính: 1 session

## Quick Commands
- Start: `/code phase-01`
- Check: `/next`
- Save: `/save-brain`
