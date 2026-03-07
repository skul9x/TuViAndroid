# 💡 BRIEF: Smart API & Model Management

**Ngày tạo:** 2026-01-31
**Dự án:** TViAI - Ứng dụng Tử Vi AI

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT

Hiện tại, ứng dụng TViAI gặp các vấn đề sau với việc quản lý Gemini API:

| Vấn đề | Hậu quả |
|--------|---------|
| Chỉ lưu được 1 API Key | Hết quota là phải vào setting nhập key mới |
| Model bị hardcode trong test | Kiểm tra kết nối không phản ánh đúng model user chọn |
| Không có fallback thông minh | Một model lỗi thì app "chết" luôn |
| Không phát hiện lỗi quota | User không biết tại sao AI không trả lời |

---

## 2. GIẢI PHÁP ĐỀ XUẤT

Xây dựng hệ thống **Smart API & Model Management** với 3 trụ cột:

- 🔑 **Multi-Key Pool**: Regex extract, auto-rotate
- 🤖 **Model Priority**: Nạc → Xương (best → fallback)
- 🔄 **Quota Detection**: Tự nhận biết 429, rotate

---

## 3. TÍNH NĂNG MVP

- [x] **Multi-API Key Support**
  - Paste cả đoạn text chứa nhiều keys
  - App tự regex trích xuất `AIza...` patterns
  - Hiển thị danh sách keys đã lưu (ẩn giữa key)

- [x] **Model Priority Fallback**
  - Thứ tự ưu tiên cố định (nạc → xương):
    1. `gemini-3-pro-preview`
    2. `gemini-2.5-pro`
    3. `gemini-3-flash-preview`
    4. `gemini-2.5-flash`
    5. `gemini-2.0-flash`
  - Hết quota model này → tự chuyển model tiếp theo

- [x] **API Key Rotation**
  - Hết quota toàn bộ models của key này → chuyển key tiếp theo

- [x] **Smart Connection Test**
  - Kiểm tra kết nối ĐÚNG model user đã chọn

- [x] **Clear Error Message**
  - Khi hết sạch: "❌ Hết Quota API"

---

## 4. BƯỚC TIẾP THEO

→ Chạy `/code` để bắt đầu implement!
