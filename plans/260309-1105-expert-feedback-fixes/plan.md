# Plan: Expert Feedback Fixes (Level 5 Refinement)
Created: 2026-03-09 11:05
Status: ✅ Complete

## Overview
Sửa 5 lỗi logic / metadata được chuyên gia Tử Vi phát hiện sau khi review prompt Level 5.

## Scope

| # | Vấn đề | Mức độ | File |
|---|--------|--------|------|
| 1 | `detectBoSao()` — Tử Phủ Vũ Tướng check sai cặp | ❌ Critical | GeminiClient.kt |
| 2 | Nhật Nguyệt — label quá chung, thiếu phân loại | ❌ Medium | GeminiClient.kt |
| 3 | Sát Phá Tham — nên ghi "Tam hợp" thay vì cách cục | ⚠️ Minor | GeminiClient.kt |
| 4 | `ĐV. H Lộc` — viết tắt khó hiểu cho AI | ⚠️ Minor | TuViLogic.kt |
| 5 | Thiếu block "Xếp hạng cách cục" khi có nhiều cách | ❌ Medium | GeminiClient.kt |

## Phases

| Phase | Name | Status | Tasks | Progress |
|-------|------|--------|-------|----------|
| 01 | Fix detectBoSao + ĐV Labels | ✅ Complete | 8 | 100% |
| 02 | Prompt Cách Cục Ranking | ✅ Complete | 4 | 100% |
| 03 | Testing & Verify | ✅ Complete | 5 | 100% |

**Tổng:** 17 tasks | Ước tính: 1 session

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
