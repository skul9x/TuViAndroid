# Phase 01: Code Fix & Test
Status: ⬜ Pending

## Objective
Sửa bug hiển thị sao rỗng.

## Requirements
### Functional
- [ ] Không còn Sub_star nào có `name: ""` trong kết quả Prompt JSON.

## Implementation Steps
1. [ ] Cập nhật `GeminiClient.kt` -> `buildPalacesJsonArray` -> Lọc các tên sao blank trước khi parse.
2. [ ] Sửa test nếu cần thiết.

## Files to Create/Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` - Lọc `name` trước khi chạy `parseStarToJson`
