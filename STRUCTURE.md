# TViAI Project Structure

Dự án TViAI được tổ chức theo kiến trúc Clean Architecture đơn giản kết hợp với Jetpack Compose (MVVM).

## 📁 Cấu trúc thư mục chính

```text
app/src/main/java/com/example/tviai/
├── core/               # Business Logic & AI Client
│   ├── Constants.kt    # Các hằng số Tử Vi (Can Chi, Ngũ Hành, Sao...)
│   ├── GeminiClient.kt # Quản lý kết nối Gemini API (Rotation, Fallback)
│   ├── LunarConverter.kt # Chuyển đổi lịch Âm - Dương
│   └── TuViLogic.kt    # Thuật toán an sao và lập lá số
├── data/               # Data Layer (Database, Preferences, Repository)
│   ├── HistoryDatabase.kt # Room Database cho lịch sử
│   ├── HistoryRepository.kt # Cầu nối dữ liệu
│   ├── Models.kt       # Các Data Class (UserInput, LasoData, CungInfo)
│   └── SettingsDataStore.kt # Lưu trữ API Keys và cấu hình app
├── navigation/         # Điều hướng màn hình
│   └── NavGraph.kt     # Định nghĩa luồng di chuyển giữa các Screen
├── ui/                 # Giao diện người dùng (Jetpack Compose)
│   ├── components/     # Các UI Components dùng chung (Button, Card, Markdown...)
│   ├── screens/        # Các màn hình chính (Input, Laso, Analysis, Settings, History)
│   └── theme/          # Cấu hình màu sắc, font chữ, theme của app
├── viewmodel/          # State Management
│   └── TuViViewModel.kt # Xử lý logic giao diện và kết nối UI với Data Layer
├── MainActivity.kt     # Entry point của ứng dụng
└── TuViApplication.kt  # Khởi tạo DI và ứng dụng
```

## 🛠 Chi tiết các thành phần quan trọng

### 1. GeminiClient.kt
Thành phần cốt lõi quản lý AI:
- Hỗ trợ nhập và trích xuất nhiều API Key cùng lúc.
- Tự động xoay vòng (rotation) API Key khi hết quota.
- Tự động fallback giữa các model (Flash 3 -> 2.5 -> Latest) để đảm bảo luôn có kết quả.

### 2. TuViLogic.kt
Trái tim của thuật toán Tử Vi:
- Tính toán Can Chi, Ngũ Hành từ ngày tháng năm sinh.
- Thực hiện an 14 chính tinh và các phụ tinh theo đúng luật Tử Vi Đẩu Số.
- Xây dựng cấu trúc 12 cung (Mệnh, Phụ, Phúc, Điền...).
<<<<<<< HEAD
=======
- **Tính toán vận hạn đa tầng**: Bao gồm Đại Vận, Tiểu Hạn, Lưu Niên Thái Tuế và **Lưu Nguyệt** (vận tháng).
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))

### 3. SettingsDataStore.kt
Quản lý cấu hình linh hoạt:
- Sử dụng Jetpack DataStore (Preferences).
- Lưu trữ danh sách API Keys an toàn dưới dạng JSON.
- Theo dõi model được chọn và các tùy chỉnh UI khác.

### 4. MarkdownText.kt
Thành phần UI tùy chỉnh để hiển thị văn bản từ AI:
- Render đúng các định dạng Markdown như `# Heading`, `**Bold**`, `*Italic*`.
- Giúp kết quả luận giải trông chuyên nghiệp và dễ đọc hơn.
```
