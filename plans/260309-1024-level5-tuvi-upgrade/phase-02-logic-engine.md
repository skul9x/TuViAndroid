# Phase 02: Logic Engine (Can Chi + Tiểu Hạn + Phi Tinh Pre-compute)
Status: ✅ Complete
Dependencies: Phase 01

## Objective
Implement 3 tính năng mới trong `TuViLogic.kt`:
1. Tính Can Chi cho 12 cung (Ngũ Dần Độn)
2. Tính cung Tiểu Hạn năm xem
3. Pre-compute Phi Tinh Tứ Hóa cho từng cung

## Files to Modify
- `app/src/main/java/com/example/tviai/core/TuViLogic.kt`

## Implementation Steps

### A. Can Chi 12 Cung (Ngũ Dần Độn)
Logic đã tồn tại rải rác trong `anCuc()` và `buildFullDaiVanList()`, chỉ cần extract & reuse.

1. [x] Tạo hàm `private fun anCanChiCung(cungList, canNamIndex)`:
   - Input: canNamIndex (0-9)
   - Logic: Ngũ Dần Độn → Tìm Can Dần → Thuận đến 12 cung
   - Output: Gán `canChi` cho từng `CungInfo` (VD: "Canh Tuất")
   ```
   Giáp/Kỷ → Bính Dần (startCan=2)
   Ất/Canh  → Mậu Dần (startCan=4)
   Bính/Tân → Canh Dần (startCan=6)
   Đinh/Nhâm → Nhâm Dần (startCan=8)
   Mậu/Quý → Giáp Dần (startCan=0)
   ```
2. [x] Gọi `anCanChiCung()` trong hàm `anSao()` sau khi init 12 cung

### B. Tiểu Hạn
3. [x] Tạo hàm `private fun tinhTieuHan(chiNamSinh, viewingYear, birthYear, gender, isThuan): Int`:
   - Tuổi Âm = viewingYear - birthYear + 1
   - Cung Tiểu Hạn:
     - Nam Thuận/Nữ Nghịch: Từ cung Chi năm sinh, đếm THUẬN theo tuổi
     - Nam Nghịch/Nữ Thuận: Từ cung Chi năm sinh, đếm NGHỊCH theo tuổi
   - Return: index cung tiểu hạn (0-11)
4. [x] Gán kết quả vào `UserInfoResult.tieuHanCung`

### C. Phi Tinh Tứ Hóa (Pre-compute)
5. [x] Tạo hàm `private fun phiTinhTuHoa(cungList): String`:
   - Với mỗi cung (12 cung):
     - Lấy Can cung → Tra `TU_HOA_MAP` → Xác định 4 sao hóa (Lộc/Quyền/Khoa/Kỵ)
     - Tìm 4 sao này nằm ở cung nào → Ghi nhận kết quả
   - Format output string (inject vào prompt):
     ```
     Cung Tuất (Canh): Phi H.Lộc→Thái Dương(Thìn), Phi H.Quyền→Vũ Khúc(Dần),
                        Phi H.Khoa→Thái Âm(Tý), Phi H.Kỵ→Thiên Đồng(Sửu)
     ```
6. [x] Gọi `phiTinhTuHoa()` trong `anSao()` và truyền kết quả string

### D. Metadata Âm/Dương
7. [x] Tính string `amDuong`:
   - Xác định Can năm Dương/Âm (chẵn/lẻ)
   - Kết hợp giới tính: "Dương Nam – Thuận hành" / "Âm Nữ – Nghịch hành"
8. [x] Gán vào `UserInfoResult.amDuong`

## Test Criteria
- [ ] Build thành công
- [ ] 11 tests cũ vẫn PASS
- [ ] Can Chi cung Tuất cho Nhâm Thân 1992 = "Canh Tuất" (kiểm tra bằng test mới)
- [ ] Phi Tinh output string không rỗng

## Notes
- Phi Tinh pre-compute giúp AI không cần tự tính → giảm hallucination
- `TU_HOA_MAP` dùng chung cho phi tinh bản mệnh, đại vận, và phi tinh cung
- Tiểu Hạn có nhiều trường phái; dùng trường phái phổ biến nhất (khởi từ Chi năm sinh)

---
Next Phase: [phase-03-prompt-engine.md](phase-03-prompt-engine.md)
