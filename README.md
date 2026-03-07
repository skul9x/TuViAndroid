# TViAI - Tử Vi & AI Luận Giải

**TViAI** là ứng dụng Android hiện đại kết hợp giữa thuật toán Tử Vi cổ điển và sức mạnh của trí tuệ nhân tạo (Gemini API) để mang đến những bản luận giải lá số sâu sắc, cá nhân hóa.

![Version](https://img.shields.io/badge/version-1.2.0-blue)
![Tech](https://img.shields.io/badge/tech-Kotlin%20%7C%20Compose%20%7C%20Gemini-orange)

## ✨ Tính năng nổi bật

- 🔭 **An sao chuẩn xác**: Thuật toán an sao tự động dựa trên ngày giờ sinh, giới tính.
- 🤖 **Luận giải AI thông minh**: Sử dụng các model Gemini (Flash 3 Preview, 2.5 Flash...) để bình giải lá số theo nhiều phong cách (Nghiêm túc, Hài hước, Kiếm hiệp...).
- 🔑 **Smart API Management**:
    - Hỗ trợ nhập hàng loạt API Keys.
    - Tự động xoay vòng Key khi hết quota.
    - Cơ chế Fallback models (nếu model cao cấp hết hạn, tự động dùng model dự phòng).
- 📅 **Năm xem hạn linh hoạt**: Cho phép chọn năm để xem vận hạn (mặc định là năm hiện tại).
- 📋 **Lịch sử & Xuất dữ liệu**: Lưu lại các lá số đã xem, hỗ trợ copy prompt để tự luận giải trên các nền tảng khác.
- 🎨 **Giao diện Premium**: Thiết kế tinh tế với chế độ tối (Dark Mode), hiệu ứng vàng kim sang trọng.

## 🛠 Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose
- **Async & Flow**: Coroutines, StateFlow
- **AI Backend**: Google Generative AI SDK (Gemini)
- **Data Persistence**: Room Database (Lịch sử), DataStore (Cấu hình)
- **Dependency Injection**: Manual (AppContainer pattern)

## 🚀 Hướng dẫn bắt đầu

### 1. Cài đặt API Key
Để sử dụng tính năng luận giải AI, bạn cần có ít nhất một Gemini API Key:
1. Truy cập [Google AI Studio](https://aistudio.google.com/) để lấy key.
2. Vào phần **Cài đặt** trong app TViAI.
3. Paste đoạn text chứa key hoặc nhiều keys (App sẽ tự trích xuất).
4. Nhấn **Lưu cấu hình**.

### 2. Xem lá số
1. Nhập Họ tên, Ngày tháng năm sinh, Giờ sinh và Giới tính.
2. Chọn **Năm xem hạn** (nếu muốn xem cho tương lai hoặc quá khứ).
3. Nhấn **Xem Lá Số & Luận Giải**.
4. Chờ AI kết nối với các vì sao và trả về bản bình giải.

## 📁 Tài liệu kỹ thuật
Xem chi tiết cấu trúc code tại [STRUCTURE.md](./STRUCTURE.md)

---
*Phát triển bởi Đội ngũ TViAI - Mang tinh tú đến gần bạn hơn.*
```
