# Phase 03: Prompt Engine — 7 Sections Phương Pháp Luận Mới
Status: ✅ Complete
Dependencies: Phase 02

## Objective
Mở rộng hàm `constructPrompt()` trong `GeminiClient.kt` với 7 section phương pháp luận Level 5 + metadata bổ sung + danh sách cách cục mở rộng.

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt`

## Implementation Steps

### A. Metadata Bổ Sung (Section 0)
1. [x] Inject Can Chi 12 cung vào Section 0:
   ```
   - Can Chi 12 cung: Tý=Nhâm Tý, Sửu=Quý Sửu, Dần=Canh Dần...
   ```
2. [x] Inject Âm/Dương mệnh + hướng đại vận:
   ```
   - Âm/Dương: Dương Nam – Thuận hành
   ```
3. [x] Inject Cung Tiểu Hạn:
   ```
   - Tiểu Hạn năm 2026: Cung Thân
   ```
4. [x] Inject Bảng Phi Tinh đã pre-compute:
   ```
   - Phi Tinh Tứ Hóa 12 cung:
     Tuất (Canh): Lộc→Thái Dương(Thìn), Quyền→Vũ Khúc(Dần)...
   ```

### B. 7 Sections Phương Pháp Luận (Thêm vào phần QUY TRÌNH PHÂN TÍCH)
5. [x] **Section "PHÂN TÍCH TỨ HÓA BẢN MỆNH (BẮT BUỘC)"**
   - Quy trình 8 bước đọc từng Hóa
   - Lộc Kỵ trùng phùng, Kỵ+sát tinh, Lộc+cát tinh
   - Hóa Kỵ rơi Mệnh/Thân → lao tâm

6. [x] **Section "PHÂN TÍCH NGŨ HÀNH 4 TẦNG (BẮT BUỘC trong Bước 2)"**
   - Mệnh/Cục/Cung/Sao ngũ hành
   - Sinh/Khắc/Đồng hành giữa các tầng

7. [x] **Section "QUY TẮC LUẬN THEO GIỚI TÍNH"**
   - Nam vs Nữ: Đào Hoa, Sát Phá Tham, Phu Thê, Cô Thần/Quả Tú
   - Hướng đại vận Thuận/Nghịch theo Âm Dương + Giới tính

8. [x] **Section "QUY TRÌNH PHÂN TÍCH TUẦN – TRIỆT"**
   - Tuần: giảm 30-50%, cát bị Tuần vs sát bị Tuần
   - Triệt: giảm 50-70%, mạnh hơn Tuần
   - Bắt buộc kiểm tra mỗi cung

9. [x] **Section "PHI TINH TỨ HÓA (Phân tích nâng cao)"**
   - Rule: Can cung A bay hóa sang cung B → mối quan hệ nhân quả
   - Hướng dẫn AI đọc kết quả phi tinh pre-compute
   - Tự hóa (cung hóa cho chính nó)

10. [x] **Section "PHÂN TÍCH VẬN HẠN ĐA TẦNG (BẮT BUỘC khi xem vận)"**
    - Bước 1: Xác định Đại Vận + Can ĐV → Tứ hóa ĐV
    - Bước 2: Dịch chuyển 12 cung (Mệnh đại vận)
    - Bước 3: Xếp chồng Lưu Niên Tứ Hóa
    - Bước 4: Tìm Trùng Điệp (Tam Kỵ, Song Lộc, Lộc Mã giao trì)
    - Bước 5: Kết luận vận

### C. Section "KIỂM CHỨNG CHÉO" + Mở rộng Cách Cục
11. [x] **Section "KIỂM CHỨNG CHÉO (Sau khi luận xong)"**
    - Tam giác: Mệnh-Quan-Tài, Mệnh-Phu Thê-Phúc Đức, Mệnh-Tật Ách-Phúc Đức
    - Đối chiếu Mệnh (bẩm sinh) vs Thân (hậu thiên)
    - Mâu thuẫn → phải giải thích, không được bỏ qua

12. [x] **Mở rộng danh sách Cách Cục** trong BƯỚC 3:
    - Đại Quý: Quân Thần Khánh Hội, Nhật Xuất Lôi Môn, Nguyệt Lãng Thiên Môn, Minh Châu Xuất Hải
    - Đại Phú: Vũ Khúc + Lộc Tồn, Song Lộc, Lộc Mã Giao Trì
    - Cách Võ: Mã Đầu Đới Kiếm, Hỏa/Linh Tham, Thất Sát triều đẩu
    - Cách Hung: Mệnh VCD+sát, Hình Tù Giáp Ấn, Lộc Phùng Xung Phá, Tam Kỵ Trùng Phùng, Nhật Nguyệt phản bối

## Test Criteria
- [ ] Build thành công
- [ ] Prompt output chứa keyword "TỨ HÓA BẢN MỆNH"
- [ ] Prompt output chứa keyword "PHI TINH TỨ HÓA"
- [ ] Prompt output chứa keyword "KIỂM CHỨNG CHÉO"
- [ ] Prompt output chứa keyword "GIỚI TÍNH"
- [ ] Prompt output chứa Can Chi 12 cung data

## Notes
- Prompt sẽ dài hơn ~2000 từ so với hiện tại → tăng token usage
- Các section sử dụng format text rõ ràng với viền `=====` để AI dễ parse
- Phi Tinh section chỉ hướng dẫn AI ĐỌC kết quả pre-compute, KHÔNG yêu cầu AI tự tính

---
Next Phase: [phase-04-testing.md](phase-04-testing.md)
