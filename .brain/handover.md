━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 HANDOVER DOCUMENT (Level-Up AI)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📍 Đang làm: Nâng cấp Logic Metadata AI (Tử Vi v3.1)
🔢 Đến bước: Hoàn thành Metadata Refinement (B->Bình & VCD) ✅

✅ ĐÃ XONG:
   - Fix 1: Chuyển ký hiệu `(B)` thành `(Bình)` để AI không bị "lag" khi gặp abbreviation lạ.
   - Fix 2: Thêm nhãn `[Vô chính diệu]` tường minh cho các cung trống chính tinh.
   - Đồng bộ `GeminiClient.kt` prompt metadata conventions.
   - Xác minh 11/11 unit tests (`DataLayerVerificationTest.kt`) — Logic & Prompt formatting chuẩn 100%.

⏳ CÒN LẠI:
   - Task: Tích hợp dữ liệu thực tế (nghề nghiệp, trạng thái hôn nhân) để AI tự đối soát (reverse-validation).
   - Task: Mở rộng phát hiện bộ sao nâng cao (Tứ Linh, Thiên Hình, v.v.).

🔧 QUYẾT ĐỊNH QUAN TRỌNG:
   - Ký hiệu `(Bình)` được ưu tiên hơn `(B)` vì reviewer lưu ý AI có thể không đoán đúng nghĩa từ viết tắt 1 chữ cái.
   - Nhãn `[Vô chính diệu]` được chèn trực tiếp vào dòng spec của Cung để AI bắt buộc phải thấy khi quét qua cung đó.

⚠️ LƯU Ý CHO SESSION SAU:
   - Toàn bộ logic hiển thị prompt nằm trong `GeminiClient.kt` hàm `constructPrompt`.
   - `TuViLogic.kt` hiện đã bao quát đầy đủ miếu vượng/đắc hãm và nạp âm nạp cục.
   - 11 Unit tests cung cấp baseline cực kỳ vững chắc để refactor tiếp theo.

📁 FILES QUAN TRỌNG:
   - app/src/main/java/com/example/tviai/core/TuViLogic.kt
   - app/src/main/java/com/example/tviai/core/GeminiClient.kt
   - app/src/test/java/com/example/tviai/core/DataLayerVerificationTest.kt
   - CHANGELOG.md (v3.1)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📍 Đã lưu! Để tiếp tục: Gõ /recap
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
