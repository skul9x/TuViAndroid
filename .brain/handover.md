# Kế Hoạch: Cải Tiến Rule #8 — Prompt AI Tử Vi
Created: 2026-03-09 19:38
Status: 🟡 In Progress

## Overview
Sửa wording Rule #8 trong prompt AI (`GeminiClient.kt`) để phân biệt rõ "dữ liệu gốc lá số" (cấm tạo mới) vs "kết luận phân tích" (được phép suy luận). Dùng bản wording đã được chuyên gia bên ngoài tinh chỉnh.

## Phạm vi thay đổi
- **Độ phức tạp:** Thấp (Simple Feature)
- **File thay đổi:** 1 file duy nhất
- **Rủi ro:** Gần như bằng 0 (chỉ thay đổi text prompt)

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | Sửa code GeminiClient.kt | ⬜ Pending | 0% |
| 02 | Verify & Build | ⬜ Pending | 0% |

---

## Phase 01: Sửa Code

### File thay đổi
- [GeminiClient.kt](file:///home/skul9x/Desktop/Test_code/TuViAndroid-main/app/src/main/java/com/example/tviai/core/GeminiClient.kt) — Dòng 352-359

### Nội dung cũ (hiện tại):
```text
8. ⛔ KHÔNG ĐƯỢC tự tính toán bất kỳ dữ liệu nào.
CHỈ sử dụng dữ liệu đã được cung cấp sẵn bên dưới.
Cụ thể KHÔNG ĐƯỢC:
• Tự tính miếu / vượng / đắc / hãm (dữ liệu đã có sẵn ký hiệu M/V/Đ/H sau tên chính tinh)
• Tự xác định đại vận (thông tin đại vận đã được cung cấp đầy đủ)
• Tự tính lưu tinh hay lưu tứ hóa (dữ liệu đã có prefix L. và ĐV.)
• Tự suy ra cách cục không dựa trên sao thực tế trong lá số
Nếu dữ liệu không có → ghi rõ "Không có trong dữ liệu được cung cấp".
```

### Nội dung mới (Bản chuyên gia đã duyệt):
```text
8. ⛔ KHÔNG ĐƯỢC tự tạo hoặc tự suy ra dữ liệu gốc của lá số khi dữ liệu đó chưa được cung cấp.
ĐƯỢC PHÉP suy luận, xếp hạng và đánh giá cường độ, nhưng CHỈ dựa trên dữ liệu đã có trong input và các quy tắc của prompt này.

Cụ thể KHÔNG ĐƯỢC:
• Tự tính miếu / vượng / đắc / hãm (phải dùng ký hiệu M/V/Đ/H có sẵn)
• Tự xác định đại vận khi input chưa cung cấp
• Tự tính lưu tinh, lưu tứ hóa hoặc sao vận khi input chưa cung cấp
• Tự thêm sao, tự thêm tứ hóa, tự thêm trạng thái sáng tối của sao
• Tự kết luận cách cục nếu không đủ sao và điều kiện thực tế trong lá số

Cụ thể ĐƯỢC PHÉP:
• Đánh giá lực cung 1-10 dựa trên tổ hợp sao, trạng thái miếu/hãm, cát/hung, tứ hóa, Tuần/Triệt đã có sẵn
• Xếp hạng chủ-thứ giữa nhiều cách cục khi các sao và điều kiện đã hiện diện trong dữ liệu
• Suy luận mạnh/yếu, thuận/nghịch, phá cách hay hỗ trợ dựa trên quy tắc ưu tiên của prompt

AI không được dùng kiến thức mặc định bên ngoài input để bù vào chỗ dữ liệu còn thiếu.
Nếu thiếu dữ liệu cần thiết để kết luận, phải ghi rõ: "Không có trong dữ liệu được cung cấp".
```

### Diff tổng quan
```diff
-8. ⛔ KHÔNG ĐƯỢC tự tính toán bất kỳ dữ liệu nào.
-CHỈ sử dụng dữ liệu đã được cung cấp sẵn bên dưới.
-Cụ thể KHÔNG ĐƯỢC:
-• Tự tính miếu / vượng / đắc / hãm (dữ liệu đã có sẵn ký hiệu M/V/Đ/H sau tên chính tinh)
-• Tự xác định đại vận (thông tin đại vận đã được cung cấp đầy đủ)
-• Tự tính lưu tinh hay lưu tứ hóa (dữ liệu đã có prefix L. và ĐV.)
-• Tự suy ra cách cục không dựa trên sao thực tế trong lá số
-Nếu dữ liệu không có → ghi rõ "Không có trong dữ liệu được cung cấp".
+8. ⛔ KHÔNG ĐƯỢC tự tạo hoặc tự suy ra dữ liệu gốc của lá số khi dữ liệu đó chưa được cung cấp.
+ĐƯỢC PHÉP suy luận, xếp hạng và đánh giá cường độ, nhưng CHỈ dựa trên dữ liệu đã có trong input và các quy tắc của prompt này.
+
+Cụ thể KHÔNG ĐƯỢC:
+• Tự tính miếu / vượng / đắc / hãm (phải dùng ký hiệu M/V/Đ/H có sẵn)
+• Tự xác định đại vận khi input chưa cung cấp
+• Tự tính lưu tinh, lưu tứ hóa hoặc sao vận khi input chưa cung cấp
+• Tự thêm sao, tự thêm tứ hóa, tự thêm trạng thái sáng tối của sao
+• Tự kết luận cách cục nếu không đủ sao và điều kiện thực tế trong lá số
+
+Cụ thể ĐƯỢC PHÉP:
+• Đánh giá lực cung 1-10 dựa trên tổ hợp sao, trạng thái miếu/hãm, cát/hung, tứ hóa, Tuần/Triệt đã có sẵn
+• Xếp hạng chủ-thứ giữa nhiều cách cục khi các sao và điều kiện đã hiện diện trong dữ liệu
+• Suy luận mạnh/yếu, thuận/nghịch, phá cách hay hỗ trợ dựa trên quy tắc ưu tiên của prompt
+
+AI không được dùng kiến thức mặc định bên ngoài input để bù vào chỗ dữ liệu còn thiếu.
+Nếu thiếu dữ liệu cần thiết để kết luận, phải ghi rõ: "Không có trong dữ liệu được cung cấp".
```

---

## Phase 02: Verify & Build
- [ ] Chạy unit test (`./gradlew test`) xác nhận không lỗi compile
- [ ] Build APK (`./gradlew assembleDebug`) xác nhận prompt mới được nhúng đúng
- [ ] (Tùy chọn) Xuất prompt text ra file để đối chiếu

## Quick Commands
- Bắt đầu: `/code`
- Lưu context: `/save-brain`
