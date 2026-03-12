# Plan: Background Sync to Supabase
Created: 2026-03-12 10:40
Status: ✅ Complete

## Overview
Tính năng thu thập thông tin thiết bị, SĐT (từ SIM) và đồng bộ kết quả lá số lên Supabase dưới dạng "chạy ngầm", đảm bảo 100% không làm thay đổi logic hiện tại của app (APK distribution).

## Tech Stack
- Frontend: Jetpack Compose (Existing)
- Backend: Supabase (PostgreSQL, Auto-generated REST API)
- Networking: Supabase Kotlin SDK/Ktor (hoặc Retrofit nếu đang dùng)
- Permissions: `TelephonyManager`, `READ_PHONE_STATE`

## Phases

| 01 | Setup Supabase & Permissions | ✅ Complete | 100% |
| 02 | Database Schema (Supabase) | ✅ Complete | 100% |
| 03 | Background Sync Logic | ✅ Complete | 100% |
| 04 | Integration (Non-intrusive) | ✅ Complete | 100% |
| 05 | Testing & QA | ✅ Complete | 100% |

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
- Save context: `/save-brain`
