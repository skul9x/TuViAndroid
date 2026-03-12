# Phase 02: Database Schema (Supabase)
Status: ⬜ Pending
Dependencies: Phase 01

## Objective
Tạo bảng `laso_sync` trên Supabase (Dashboard manual hoặc SQL) để nhận dữ liệu từ app Android.

## Requirements
### Functional
- [ ] Thiết kế bảng PostgreSQL: `id` (uuid), `phone_number` (text), `device_info` (jsonb), `laso_data` (jsonb), `created_at` (timestamp).
- [ ] Cấu hình Row Level Security (RLS) để cho phép app insert data.

### Non-Functional
- [ ] Tối ưu kiểu dữ liệu jsonb cho query sau này.

## Implementation Steps
1. [ ] Cung cấp script SQL cho người dùng chạy trên Supabase Dashboard.
2. [ ] Hoặc hướng dẫn người dùng tạo bảng qua giao diện Supabase.
3. [ ] Hướng dẫn setup RLS policies (Thường là cho phép Insert Anon).

## Files to Modify/Create
- `[NEW] docs/supabase_schema.sql` (Script SQL để user copy-paste).

## Test Criteria
- [ ] Bảng được tạo thành công trên Supabase.
- [ ] Có thể curl/POST một dòng test từ terminal lên Supabase.

---
Next Phase: `/code phase-03`
