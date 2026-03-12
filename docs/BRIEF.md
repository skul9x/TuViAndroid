# 💡 BRIEF: Dự án Đồng bộ Lá số & Telemetry (Supabase Integration)

**Ngày tạo:** 2026-03-12
**Trạng thái:** Finalized (APK Delivery)

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT
- Lưu trữ dữ liệu lá số lên Supabase để quản lý tập trung.
- Thu thập SĐT để marketing bản Pro sau này.
- Thu thập thông tin thiết bị (Telemetry) để debug.

## 2. GIẢI PHÁP ĐỀ XUẤT (Chạy ngầm 100%)
- **Cơ chế:** Khi nhấn nút "Xem lá số", app vẫn thực hiện các chức năng cũ bình thường. Một luồng xử lý ngầm (Background Task) sẽ được kích hoạt để gửi data lên Supabase mà không làm gián đoạn hay delay UI của người dùng.
- **Dữ liệu đồng bộ:**
    1. Thông tin lá số (JSON).
    2. Thông tin thiết bị (Model, Brand, SDK Version).
    3. Số điện thoại (Đọc trực tiếp từ SIM).

## 3. ĐỐI TƯỢNG SỬ DỤNG
- Người dùng app TuViAndroid hiện tại.

## 4. TÍNH NĂNG
### 🚀 MVP (Cài đặt chạy ngầm):
- [ ] Tích hợp Supabase SDK.
- [ ] Logic lấy thông tin máy & SĐT (Cần quyền `READ_PHONE_STATE`).
- [ ] Worker chạy ngầm để đẩy data lên Supabase.
- [ ] Đảm bảo KHÔNG thay đổi bất kỳ UI hay logic hiện có nào của app.

## 5. ĐÁNH GIÁ KỸ THUẬT
- **Phân phối:** APK trực tiếp (Không qua Google Play).
- **Chiến thuật thu thập SĐT:** Dùng `TelephonyManager` để đọc trực tiếp.
- **Ràng buộc:** Nếu không có quyền hoặc SIM không có số, vẫn cho phép xem lá số bình thường (ghi nhận SĐT = "Unknown").

## 6. BƯỚC TIẾP THEO
→ Thực hiện theo [implementation_plan.md](file:///C:/Users/Administrator/.gemini/antigravity/brain/2c1f28f8-e284-4851-bd3f-78ed5bc38f93/implementation_plan.md).
