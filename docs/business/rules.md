# Quy Tắc Nghiệp Vụ Tử Vi (Business Rules)

Tài liệu này lưu lại các quy tắc logic đã được kiểm chứng và thống nhất trong dự án TViAI.

## 1. Hệ Thống Cung
- **Chiều gán tên Cung:** Thuận chiều kim đồng hồ (+1 index).
  - Thật tự: Mệnh (0) -> Phụ Mẫu (1) -> Phúc Đức (2) -> Điền Trạch (3) -> Quan Lộc (4) -> Nô Bộc (5) -> Thiên Di (6) -> Tật Ách (7) -> Tài Bạch (8) -> Tử Tức (9) -> Phu Thê (10) -> Huynh Đệ (11).
- **Cung Thân:** An dựa trên Tháng và Giờ sinh (Thân luôn đi kèm với 1 trong 6 cung: Mệnh, Phúc, Quan, Di, Tài, Phối).

## 2. Hệ Thống Chính Tinh
- **Thiên Phủ:** Đối xứng với Tử Vi qua trục **Dần-Thân** (Tổng index = 4).
  - Công thức: `pos = (4 - tuViPos) % 12`.
- **Vòng Tử Vi (Nghịch):** Tử Vi (0) -> Thiên Cơ (-1) -> Thái Dương (-3) -> Vũ Khúc (-4) -> Thiên Đồng (-5) -> Liêm Trinh (-8).
- **Vòng Thiên Phủ (Thuận):** Thiên Phủ (0) -> Thái Âm (1) -> Tham Lang (2) -> Cự Môn (3) -> Thiên Tướng (4) -> Thiên Lương (5) -> Thất Sát (6) -> Phá Quân (10).

## 3. Hệ Thống Phụ Tinh Đặc Biệt
- **Văn Khúc / Văn Xương:**
  - An theo Giờ sinh cho Sao Cố Định.
  - An theo Can Năm cho **Lưu Văn Khúc / Lưu Văn Xương** và **Đại Vận Văn Khúc / Văn Xương**.
  - Lưu ý: VAN_KHUC_MAP cho Can Nhâm là cung Thân (8) và Can Quý là cung Hợi (11).
- **Thiên Y / Thiên Riêu:** 
  - An theo Tháng sinh (Thiên Y luôn đồng cung với Thiên Riêu).
- **Tứ Hóa (Lộc, Quyền, Khoa, Kỵ):** 
  - An theo Can của Năm (Năm sinh, Năm xem, hoặc Năm Đại vận).

## 4. Hệ Thống Điểm & Đánh Giá
- **Thứ tự thực hiện:** 
  1. An sao.
  2. Tính điểm (Score).
  3. Gán độ sáng (M, V, Đ, H).
- **Lý do:** Tránh việc các ký tự độ sáng (M), (V) làm hỏng việc tra cứu điểm trong bảng `STAR_SCORES`.

## 5. Các Chỉnh Sửa Đặc Biệt (User Requests)
- **Văn Tinh:** Cố định tại Dậu (9).
- **Loại bỏ sao nhiễu:** Không hiển thị L.Hồng Hỷ và các sao nhỏ gây rối mắt trên lá số trung tâm.
