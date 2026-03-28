# TViAI - Ứng dụng Tử Vi AI (Android)

TViAI là một ứng dụng xem lá số Tử Vi trên nền tảng Android, kết hợp với trí tuệ nhân tạo (Gemini AI) để đem đến những luận giải mệnh lý chuyên sâu, chính xác và đa phong cách.

## 🚀 Chức Năng Chính

* **Lập Lá Số Tử Vi**: Tính toán và hiển thị lá số dựa trên thông tin ngày tháng năm sinh (hỗ trợ cả Dương lịch và Âm lịch), giờ sinh, giới tính.
* **Luận Giải AI**: Tích hợp Google Gemini AI đa tính cách (Nghiêm túc, Hài hước, Chuyên gia mệnh lý, v.v.) để luận giải các cung tấu, đại vận và lưu nguyệt.
* **Smart API & Model Management**: 
    - Quản lý nhiều API Keys (quay vòng khi hết quota).
    - Tự động hạ cấp (fallback) xuống Model thấp hơn khi Model cao bị lỗi hoặc quá tải.
    - An toàn lưu trữ cấu hình trong DataStore.
* **Phi Tinh Lưu Nguyệt**: Phân tích chi tiết 12 tháng hạn vận trong năm.
* **Giao Diện Premium**: Thiết kế trực quan, mượt mà và thân thiện với người dùng di động.

## 🛠️ Cấu Trúc Dự Án

Dự án được viết bằng Kotlin với cấu trúc tiêu chuẩn của Android:

* Tầng `ui/`: Các giao diện người dùng (Jetpack Compose).
* Tầng `core/`: Xử lý lõi, tính toán thiên can địa chi, chuyển đổi Âm/Dương lịch (`LunarConverter`, `TuViLogic`), và gọi API Gemini.
* Tầng `data/`: Định nghĩa các cấu trúc dữ liệu (`UserInput`, `LasoData`) và quản lý bộ nhớ cục bộ (`SettingsDataStore`, `HistoryRepository`).
* Tầng `viewmodel/`: Quản lý luồng và trạng thái dữ liệu (StateFlow) cho các Compose screen.

## ⚙️ Hướng Dẫn Cài Đặt và Chạy

1. Yêu cầu hệ thống: Android Studio Koala hoặc muộn hơn.
2. Clone repository về máy:
   ```bash
   git clone https://github.com/skul9x/TuViAndroid.git
   ```
3. Mở Android Studio và `Sync Project with Gradle Files`.
4. Build APK qua Menu: `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
5. Đưa ứng dụng vào máy giả lập hoặc điện thoại Android thực tế và trải nghiệm.

## 🔒 Lưu Ý Cấu Hình

Dự án này sử dụng mô hình tự mang API (BYOK - Bring Your Own Key). Người dùng (hoặc người kiểm thử) phải truy cập màn hình Cài Đặt (Settings) trong ứng dụng để nhập Gemini API Key của chính mình trước khi sử dụng tính năng luận AI. Dự án không đi kèm với bất kỳ Key hardcode nào vì lý do bảo mật.

---

*Copyright 2026 Nguyễn Duy Trường*
