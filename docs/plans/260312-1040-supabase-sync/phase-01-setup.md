# Phase 01: Setup Supabase & Permissions
Status: ⬜ Pending
Dependencies: None

## Objective
Tích hợp SDK, khai báo quyền cần thiết và đảm bảo app sẵn sàng giao tiếp với Supabase.

## Requirements
### Functional
- [ ] Khai báo quyền `INTERNET`.
- [ ] Khai báo quyền `READ_PHONE_STATE` và `READ_PHONE_NUMBERS` trong `AndroidManifest.xml`.
- [ ] Tích hợp Supabase Kotlin SDK (hoặc HTTP Client tương đương) vào `build.gradle.kts`.
- [ ] Tạo `SupabaseClient.kt` chứa cấu hình URL và Anon Key.

### Non-Functional
- [ ] Xin quyền Runtime Permission (Telephony) mượt mà khi mở app mà không gián đoạn user.

## Implementation Steps
1. [ ] Cập nhật `AndroidManifest.xml`.
2. [ ] Thêm dependencies vào `build.gradle.kts`.
3. [ ] Tạo file `SupabaseClient.kt` với cấu hình cơ bản.
4. [ ] Viết helper class `PermissionHelper.kt` để quản lý việc xin quyền.

## Files to Modify/Create
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
- `[NEW] app/src/main/java/.../SupabaseClient.kt`
- `[NEW] app/src/main/java/.../PermissionHelper.kt`

## Test Criteria
- [ ] Build thành công.
- [ ] App khởi chạy và hiện thông báo xin quyền (hoặc tự động bỏ qua nếu đã cấp).
- [ ] Khởi tạo `SupabaseClient` không bị crash.

---
Next Phase: `/code phase-02`
