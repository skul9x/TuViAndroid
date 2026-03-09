# Plan: Level 5 Prompt Patch (Expert Review Fixes)
Created: 2026-03-09 11:36
Status: ✅ Complete

## Overview
Bổ sung 6 cải tiến prompt theo feedback từ 2 chuyên gia Tử Vi, đưa prompt từ Level 4.5 lên Level 5.
**Không thay đổi logic tính toán** — chủ yếu thêm text vào prompt + 1 đoạn aggregate data nhỏ.

## Scope

| # | Hạng mục | File | Loại thay đổi |
|---|----------|------|---------------|
| A | Quy tắc ưu tiên khi mâu thuẫn | GeminiClient.kt | Prompt text |
| B | Trọng số tương tác (đồng cung > tam hợp > xung chiếu) | GeminiClient.kt | Prompt text |
| C | Mở rộng quy tắc Vô chính diệu (4 bước) | GeminiClient.kt | Prompt text |
| D | Negative Examples (ví dụ SAI) | GeminiClient.kt | Prompt text |
| E | Chấm điểm LỰC CUNG [1-10] + ĐỘ TIN CẬY | GeminiClient.kt | Prompt text |
| F | Block tóm tắt ĐV Tứ Hóa + Lưu Tứ Hóa tường minh | GeminiClient.kt | Code + Prompt |

## Phases

| Phase | Name | Status | Tasks | Progress |
|-------|------|--------|-------|----------|
| 01 | Prompt Text Patches (A–E) | ✅ Complete | 5 | 100% |
| 02 | Tứ Hóa Summary Block (F) | ✅ Complete | 3 | 100% |
| 03 | Testing & Verify | ✅ Complete | 4 | 100% |

**Tổng:** 12 tasks | Ước tính: 1 session ngắn

## Quick Commands
- Start: `/code phase-01`
- Check progress: `/next`
