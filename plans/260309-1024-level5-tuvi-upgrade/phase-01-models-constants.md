# Phase 01: Data Models & Constants
Status: ✅ Complete
Dependencies: Không

## Objective
Mở rộng data models để chứa thông tin Can Chi 12 cung, Âm Dương, và Tiểu Hạn. Bổ sung danh sách Cách Cục mở rộng vào Constants.

## Files to Create/Modify
- `app/src/main/java/com/example/tviai/data/Models.kt` — Thêm fields mới
- `app/src/main/java/com/example/tviai/core/Constants.kt` — Thêm CACH_CUC danh sách

## Implementation Steps

### Models.kt
1. [x] Thêm `val canChi: String = ""` vào `CungInfo` — Can Chi của cung (VD: "Canh Tuất")
2. [x] Thêm `val amDuong: String = ""` vào `UserInfoResult` — VD: "Dương Nam – Thuận hành"
3. [x] Thêm `val tieuHanCung: String = ""` vào `UserInfoResult` — Cung tiểu hạn năm xem

### Constants.kt
4. [x] Thêm `CACH_CUC_DAI_QUY` — List cách cục đại quý (Quân Thần Khánh Hội, Phủ Tướng Triều Viên, Tử Phủ Vũ Tướng, Nhật Nguyệt Tịnh Minh, Nhật Xuất Lôi Môn, Nguyệt Lãng Thiên Môn, Minh Châu Xuất Hải)
5. [x] Thêm `CACH_CUC_DAI_PHU`, `CACH_CUC_VO`, `CACH_CUC_HUNG`, `CACH_CUC_DAC_BIET` — Lists tương ứng

## Test Criteria
- [ ] Build thành công (không compile error)
- [ ] 11 tests hiện tại vẫn PASS (backward compatible vì dùng default value)

## Notes
- Tất cả fields mới đều có default value rỗng → không break code hiện tại
- `TU_HOA_MAP` đã có sẵn và đúng → không cần sửa, dùng luôn cho Phi Tinh

---
Next Phase: [phase-02-logic-engine.md](phase-02-logic-engine.md)
