# Tử Vi AI (TuViAndroid)

Ứng dụng dự đoán, luận giải Tử Vi chuyên sâu thông minh, kết hợp các thuật toán an sao chính xác tuyệt đối (Julian Day Number) với sức mạnh phân tích ngôn ngữ tự nhiên từ **Gemini AI**.

## 🚀 Tính Năng Chính
- **Luận giải cá nhân hóa:** Lấy lá số tử vi dựa trên giới tính, ngày tháng năm sinh (Dương lịch/Âm lịch).
- **Phân tích đa không gian/thời gian:** Hỗ trợ luận giải theo Đại Vận, Lưu Niên, Lưu Nguyệt, và đặc biệt là **Lưu Nhật** (xem hạn theo ngày) cực kỳ chính xác.
- **Tương tác AI trực tiếp:** Sử dụng bộ prompt tối ưu theo chuẩn phong thủy để AI phân tích chi tiết, logic các cung và sao trên lá số.
- **Phong cách đa dạng:** Hỗ trợ nhiều ngữ điệu phân tích: Nghiêm túc, Tích cực, Thấu cảm...

## 🛠️ Công Nghệ Sử Dụng
- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Kiến trúc:** MVVM
- **AI Integration:** Google Gemini API
- **Tooling:** Gradle, Android Studio

## 📂 Cấu Trúc Dự Án
```text
TuViAndroid/
├── app/
│   ├── src/main/java/com/example/tviai/
│   │   ├── core/      # Xử lý Engine Tử Vi, cấu hình Gemini, tính JDN 
│   │   ├── data/      # Các Data Models, Constants, UserInput
│   │   ├── ui/        # Các màn hình Compose (InputScreen, ResultScreen, Components...)
│   │   └── viewmodel/ # State holders, xử lý logic UI 
├── .brain/            # Lưu trữ dữ liệu cấu hình memory của agent, lịch sử session
└── README.md          # Tài liệu dự án
```

## ⚙️ Hướng Dẫn Cài Đặt
1. **Clone dự án:**
   ```bash
   git clone https://github.com/skul9x/TuViAndroid.git
   cd TuViAndroid
   ```
2. **Cấu hình API Key:**
   - Tạo file `local.properties` tại thư mục gốc của dự án.
   - Thêm dòng: `GEMINI_API_KEY=your_google_gemini_api_key`
3. **Build & Chạy:**
   Mở dự án trong **Android Studio**, đợi Gradle sync hoàn tất, chọn máy ảo hoặc máy thật và ấn Run (Shift + F10).

## 📝 Cách Sử Dụng
1. Nhập thông tin ngày tháng năm sinh, giờ sinh và giới tính.
2. Chọn thời gian xem (Theo năm, theo tháng hoặc theo ngày).
3. Ấn **Xem Lá Số & Luận Giải**. AI sẽ sử dụng dữ liệu từ Mệnh cục, đại vận đến tiểu hạn, lưu nguyệt, lưu nhật để phân tích tương lai của bạn.

---

**Bản Quyền**
Copyright 2026 Nguyễn Duy Trường
