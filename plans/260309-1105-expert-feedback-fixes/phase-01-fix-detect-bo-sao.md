# Phase 01: Fix detectBoSao + ĐV Labels
Status: ✅ Complete
Dependencies: None

## Objective
Sửa logic detect bộ sao sai và chuẩn hóa ký hiệu Đại Vận Tứ Hóa.

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` — detectBoSao()
- `app/src/main/java/com/example/tviai/core/TuViLogic.kt` — ĐV Tứ Hóa labels

## Implementation Steps

### A. Viết lại detectBoSao() (GeminiClient.kt:592-650)

**Vấn đề hiện tại:** Hàm `isTamHopOrSame(a, b)` chỉ check 2 cung có tam hợp không, nhưng **không đảm bảo TẤT CẢ sao cùng nằm trong 1 mạng tam hợp**.

Ví dụ lỗi:
- Tử Vi ở Thìn, Thiên Phủ ở Tý → tam hợp ✓
- Vũ Khúc ở Tý, Thiên Tướng ở Thìn → tam hợp ✓
- Nhưng chỉ có 2 cung (Thìn, Tý), không phải 4 cung phân bố → **Không phải cách chuẩn**

**Logic mới:**

1. [x] Viết helper `tamHopGroup(idx: Int): Set<Int>` trả về 3 cung tam hợp
2. [x] Viết helper `getHoiHopIndices(indices: Int): Set<Int>`
3. [x] **Sát Phá Tham** — Sửa label
4. [x] **Tử Phủ Vũ Tướng** — Viết lại hoàn toàn
5. [x] **Cơ Nguyệt Đồng Lương** — Sửa tương tự Tử Phủ Vũ Tướng.
6. [x] **Nhật Nguyệt** — Phân loại cụ thể
7. [x] Sửa ký hiệu ĐV Tứ Hóa (TuViLogic.kt:871)
8. [x] Cập nhật Prompt Ký Hiệu (GeminiClient.kt:581)

## Notes
- Logic Tử Vi chuẩn: "Bộ sao" chỉ hình thành khi các sao nằm trên **cùng 1 mạng tam phương tứ chính**.
- Đồng cung ≠ Tam hợp. Phải phân biệt rõ.

---
Next Phase: phase-02 (Prompt Cách Cục Ranking)
