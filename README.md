# TuViAndroid 🔮

TuViAndroid là một ứng dụng di động xem Tử Vi nâng cao, được phát triển trên nền tảng Android. Ứng dụng kết hợp giữa việc tự động an sao, lập lá số theo phương pháp cổ truyền (Nam Phái) và trí tuệ nhân tạo (Gemini API) để phân tích, luận giải chi tiết.

## Chức Năng Nổi Bật 🚀
- **An Sao Tự Động:** Tự động tính toán và an chính tinh, phụ tinh dựa trên thuật toán Tử Vi Đẩu Số. Hỗ trợ quy tắc Vô Chính Diệu, đánh giá lực độ mạnh yếu của sao.
- **Phân Tích Bằng AI (Gemini):** Prompt JSON nâng cao giúp AI phân tích luận giải chính xác theo nhiều phong cách (Nghiêm túc, Hài hước, Chuyên gia...). Hạn chế tối đa tình trạng thao túng thông tin hay tự bịa đặt.
- **Hạn Vận Đa Tầng:** Hỗ trợ luận giải theo Đại Vận (10 năm), Lưu Niên (Năm), Lưu Nguyệt (Tháng), và Lưu Nhật (Ngày).
- **Lưu Trữ Lịch Sử Nâng Cao:** Tự động lưu trữ các lá số đã xem. Click xem lại sẽ tự động điền sẵn (pre-fill) form thông tin ban đầu, cho phép tuỳ chỉnh lại linh hoạt.
- **Chế Độ Xem Cho Trẻ Em:** Ứng xử thông minh bằng cách bỏ qua phân tích sự nghiệp/tình duyên với những lá số của trẻ em dưới 13 tuổi, tập trung vào phụ mẫu và sức khoẻ.

## Công Nghệ Sử Dụng 🛠
- **Ngôn Ngữ:** Kotlin 100%
- **Giao Diện (UI):** Jetpack Compose, Material Design 3
- **Kiến Trúc:** MVVM, Clean Architecture
- **Xử lý Bất Đồng Bộ:** Kotlin Coroutines & Flow
- **Lưu trữ Cục Bộ (Local Storage):** Room Database / SQLite
- **AI Integration:** Google Gemini REST API / JSON Prompting

## Cấu Trúc Thư Mục 📂
- `app/src/main/java/com/example/tviai/core`: Chứa logic tử vi thuần tuý (`TuViLogic.kt`, `LunarConverter.kt`) và Client kết nối API AI (`GeminiClient.kt`). 
- `app/src/main/java/com/example/tviai/data`: Các models, Repository, Room Entities cho Database và Settings.
- `app/src/main/java/com/example/tviai/ui`: Chứa màn hình (`screens`) và các thành phần giao diện dùng chung (`components`).
- `app/src/main/java/com/example/tviai/viewmodel`: Nơi quản lý trạng thái, vòng đời của view và xử lý hành động (Actions).
- `.brain`: Bộ nhớ mô hình AI ghi lại phân tích, thay đổi, log phiên làm việc để duy trì trạng thái ngữ cảnh dự án khi được AI hỗ trợ.

## Hướng Dẫn Cài Đặt & Chạy Dự Án 💻
1. Clone dự án về máy:
   ```bash
   git clone https://github.com/skul9x/TuViAndroid.git
   ```
2. Mở dự án thông qua **Android Studio**.
3. Tại thư mục gốc `local.properties`, thêm API Key của Gemini nếu có:
   ```properties
   GEMINI_API_KEY="AIzaSy...your-api-key"
   sdk.dir=C\:\\Users\\admin\\AppData\\Local\\Android\\Sdk # (tuỳ biến tuỳ HĐH)
   ```
4. Chờ Gradle Sync hoàn tất, có thể chọn cấu hình App và ấn Run để Build APK sang máy ảo hoặc máy vật lý.
5. Để build riêng APK qua command line:
   ```bash
   ./gradlew assembleDebug
   ```

## Bản Quyền 📜
Copyright 2026 Nguyễn Duy Trường
