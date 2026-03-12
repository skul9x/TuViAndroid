# Phase 03: Background Sync Logic
Status: ⬜ Pending
Dependencies: Phase 01, Phase 02

## Objective
Viết logic đẩy dữ liệu lên bảng `laso_sync` mà không chặn UI main thread.

## Requirements
### Functional
- [ ] Tạo `TelemetryRepository.kt` để đóng gói logic Gửi Supabase.
- [ ] Viết hàm lấy thông tin thiết bị (Model, Brand, SDK) qua `Build` class.
- [ ] Lấy số điện thoại từ `TelephonyManager`.

### Non-Functional
- [ ] Fallback: Nếu không lấy được SIM hoặc user từ chối quyền, gửi `Unknown` thay vì crash app.
- [ ] Serialize dữ liệu JSON an toàn trước khi gửi.

## Implementation Steps
1. [ ] Viết hàm `getDeviceTelemetry()` trong `TelemetryRepository` (hoặc class helper riêng `DeviceInterceptor.kt`).
2. [ ] Viết hàm `syncLasoData(lasoJson, phone, device)` gọi Supabase API.
3. [ ] Xử lý error (Try/Catch) để không bao giờ throw lỗi ra UI.

## Files to Modify/Create
- `[NEW] app/src/main/java/.../domain/TelemetryRepository.kt`
- `[NEW] app/src/main/java/.../util/DeviceInterceptor.kt`

## Test Criteria
- [ ] Có thể trigger hàm sync từ một Nút Test (Log ra Console hoặc nhìn Dashboard Supabase) thành công.
- [ ] Khởi chạy trên emulator không cắm SIM → Gửi 'Unknown'.

---
Next Phase: `/code phase-04`
