# TuViAndroid (Tử Vi AI)

TuViAndroid là một ứng dụng Android hiện đại được thiết kế để lập lá số Tử Vi và cung cấp các lời giải (interpretation) chi tiết bằng trí tuệ nhân tạo (Gemini AI). Ứng dụng kết hợp giữa tinh túy của khoa học huyền học phương Đông cổ điển với sức mạnh của mô hình ngôn ngữ lớn (LLM) để mang lại cái nhìn sâu sắc về vận mệnh.

## 🌟 Tính năng chính

- **Lập lá số Tử Vi chính xác**: Tính toán đầy đủ 12 cung, chính tinh, phụ tinh, tứ hóa, tuần triệt, đại vận, tiểu hạn... theo hệ thống tinh hệ cổ điển.
- **Bát Tự (BaZi) Pro Engine v4.0**: Hệ thống tính Tứ Trụ chuyên sâu với độ chính xác thiên văn (dựa trên 24 Tiết khí, Lập Xuân, và Giờ Mặt Trời Thật - TST tại kinh độ 105.8°E).
- **Luận giải bằng AI (Gemini)**: Tích hợp Google Gemini API để phân tích lá số theo nhiều phong cách (Đời thường, Nghiêm túc, Chuyên gia...). Phân tích kết hợp giữa Tử Vi và Bát Tự để đưa ra nhận định vượng suy, dụng thần.
- **Luận giải đa tầng**: Bao gồm phân tích bản mệnh, vận hạn năm hiện tại và chi tiết vận hạn 12 tháng (Lưu Nguyệt).
- **Giao diện hiện đại**: Xây dựng bằng Jetpack Compose, hỗ trợ Dark Mode và các hiệu ứng động mượt mà.
- **Hỗ trợ Lưu Nguyệt**: Tính toán và hiển thị các sao lưu theo tháng (Lưu Nguyệt) một cách chi tiết.

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt (Dùng trong TuViApplication)
- **Database**: Room Persistence (Lưu lịch sử và dữ liệu lá số)
- **AI Integration**: Google Generative AI SDK (Gemini)
- **Data Storage**: DataStore (Lưu cài đặt người dùng)
- **Network**: Retrofit/OkHttp (Giao tiếp với AI API)

## 📁 Cấu trúc dự án

- `app/src/main/java/com/example/tviai/core`: Chứa logic lõi về Tử Vi (Lịch âm dương, An sao, Tứ hóa).
- `app/src/main/java/com/example/tviai/core/BaZiLogic.kt`: Core engine tính Tứ Trụ, Thập Thần và Ngũ Hành theo tiết khí.
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt`: Xử lý giao tiếp với Gemini AI và xây dựng Prompt chuyên sâu (Optimization v3.0).
- `app/src/main/java/com/example/tviai/data`: Các Repo, DAO và Data mẫu cho ứng dụng.
- `app/src/main/java/com/example/tviai/ui`: Chứa các màn hình (screens) và components xây dựng bằng Compose.
- `app/src/main/java/com/example/tviai/viewmodel`: Quản lý trạng thái và logic nghiệp vụ cho UI.

## 🚀 Hướng dẫn cài đặt

1.  Clone repository: `git clone https://github.com/skul9x/TuViAndroid.git`
2.  Mở dự án bằng **Android Studio (Ladybug hoặc mới hơn)**.
3.  Cung cấp Gemini API Key trong phần cài đặt của ứng dụng hoặc tệp cấu hình.
4.  Build và chạy trên thiết bị Android của bạn.

---
*Dự án đang trong quá trình phát triển và hoàn thiện các tính năng phân tích chuyên sâu.*
