# 📋 HANDOVER DOCUMENT - Tử Vi AI Upgrade

**📍 Đang làm:** Nâng cấp Prompt Hệ thống & Chế độ xem theo Tháng
**🔢 Đến bước:** Hoàn thiện Phase 2 & Verify thành công (Build OK)

---

## ✅ ĐÃ XÔNG:
- **Prompt v3 Implementation**: Thay thế toàn bộ logic tạo prompt cũ bằng cấu trúc luận 12 cung chuyên sâu. Hỗ trợ Tam hợp, Xung chiếu, Giáp cung, Tứ hóa.
- **Monthly Viewing Mode**: Thêm RadioButton để chuyển đổi giữa xem vận theo Năm và theo Tháng.
- **Auto-Month Logic**: Tự động gợi ý tháng tiếp theo (Current Month + 1) khi chuyển sang chế độ tháng.
- **Model Upgrades**: Cập nhật `Models.kt`, `TuViViewModel.kt`, `TuViLogic.kt` để hỗ trợ truyền tham số tháng/năm dynamic.
- **Verification**: Build dự án thành công với `gradlew assembleDebug`.

---

## ⏳ CÒN LẠI (Gợi ý cho session sau):
- **Deep Testing**: Kiểm tra kỹ output của AI cho từng cung để đảm bảo không bị "hallucination" quá đà.
- **UI Refinement**: Có thể thêm hiệu ứng chuyển cảnh mượt hơn khi ẩn/hiện bộ chọn Tháng.
- **Internationalization**: Chuẩn bị cho việc hỗ trợ đa ngôn ngữ nếu cần (hiện tại prompt là tiếng Việt).

---

## 🔧 QUYẾT ĐỊNH QUAN TRỌNG:
- **Consistent Depth**: Tất cả "Phong cách luận giải" đều dùng Prompt v3 để đảm bảo chất lượng nội dung, chỉ thay đổi "giọng điệu" (tone of voice).
- **Lunar Focus**: Các tháng xem vận được mặc định hiểu là tháng Âm Lịch (phù hợp với bản chất Tử Vi).

---

## 📁 FILES QUAN TRỌNG:
- `GeminiClient.kt`: Chứa template Prompt v3 phức tạp.
- `InputScreen.kt`: Chứa logic UI chọn Năm/Tháng.
- `docs/DESIGN.md`: Bản thiết kế chi tiết vừa hoàn thiện.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📍 Đã lưu checkpoint! Để tiếp tục: Gõ `/recap`
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
