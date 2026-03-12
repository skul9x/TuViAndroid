# Phase 04: Integration (Non-intrusive)
Status: ⬜ Pending
Dependencies: Phase 03

## Objective
Gắn hook vào chức năng "Xem lá số" hiện tại của app Android (fire-and-forget).

## Requirements
### Functional
- [ ] Xác định vị trí lưu lá số local thành công trong `LasoViewModel` hoặc `LasoRepository`.
- [ ] Gọi `TelemetryRepository.syncLasoData()` ngầm ngay sau đó.

### Non-Functional
- [ ] Đảm bảo 100% việc sync sử dụng Coroutine (Dispatchers.IO) để không ảnh hưởng luồng Main/UI.
- [ ] UI phản hồi ngay tức thì kết quả bốc lá số như cũ.

## Implementation Steps
1. [ ] Phân tích file (ví dụ `LasoViewModel.kt` hoặc UI Screen).
2. [ ] Inject `TelemetryRepository` (Thông qua Hilt/Koin hoặc manual).
3. [ ] Gắn `viewModelScope.launch(Dispatchers.IO)` để gọi sync API mà k await.

## Files to Modify/Create
- Tùy thuộc cấu trúc app hiện hành (Thường là `LasoViewModel.kt` hoặc `MainScreen.kt`).

## Test Criteria
- [ ] Bấm nút "Xem lá số". App vẽ lá số bình thường.
- [ ] Trạng thái ngầm của Supabase nhận được 1 row dữ liệu (Check dashboard).

---
Next Phase: `/code phase-05`
