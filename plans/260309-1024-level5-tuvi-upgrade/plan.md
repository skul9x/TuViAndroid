# Plan: Nâng Cấp Tử Vi AI Lên Level 5
Created: 2026-03-09 10:24
Status: 🟡 In Progress

## Overview
Nâng cấp prompt engine và data layer của app Tử Vi AI từ Level 3-4 lên Level 5 (Phi Tinh Tứ Hóa + Tổng Hợp Đa Tầng). Bổ sung 7 module phương pháp luận mới vào prompt, pre-compute Phi Tinh Tứ Hóa, và thêm Tiểu Hạn.

## Quyết Định Đã Xác Nhận
- ✅ Giữ đầy đủ 7 section phương pháp luận (không cắt giảm)
- ✅ Pre-compute Phi Tinh Tứ Hóa sẵn trong code (Option B - chính xác hơn)
- ✅ Thêm Tiểu Hạn (làm luôn)

## Tech Stack
- Language: Kotlin
- Key Files: `Models.kt`, `Constants.kt`, `TuViLogic.kt`, `GeminiClient.kt`
- Tests: `DataLayerVerificationTest.kt`

## Phases

| Phase | Name | Status | Tasks | Progress |
|-------|------|--------|-------|----------|
| 01 | Data Models & Constants | ✅ Complete | 5 | 100% |
| 02 | Logic Engine (Can Chi + Tiểu Hạn + Phi Tinh) | ✅ Complete | 8 | 100% |
| 03 | Prompt Engine (7 Sections Mới) | ✅ Complete | 10 | 100% |
| 04 | Testing & Verification | ✅ Complete | 6 | 100% |

**Tổng:** 29 tasks | Ước tính: 2-3 sessions

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
- Save context: `/save-brain`
