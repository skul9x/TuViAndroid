# 🎨 DESIGN: Nâng Cấp App Tử Vi AI

**Ngày:** 04/03/2026
**Dựa trên:** Yêu cầu mới nhất của User (áp dụng Prompt 3 cho TẤT CẢ các phong cách luận giải).

---

## 1. Cách Lưu Thông Tin (Database / Data Model)

### Thay đổi trong `Models.kt`

Để hỗ trợ xem theo Tháng, ta cần thêm `ViewingMode` và các biến liên quan vào cấu trúc dữ liệu hiện tại (Room & DataStore không bị ảnh hưởng trực tiếp, chỉ State).

```kotlin
// Thêm Enum mới
enum class ViewingMode(val displayName: String) {
    YEAR("Theo năm"),
    MONTH("Theo tháng")
}

// Cập nhật UserInput
data class UserInput(
    ... // giữ nguyên
    val viewingYear: Int,
    val viewingMonth: Int = 0, // Dùng khi mode = MONTH
    val viewingMode: ViewingMode = ViewingMode.YEAR,
    val readingStyle: ReadingStyle = ReadingStyle.NGHIEM_TUC,
    ... // giữ nguyên
)

// Cập nhật UserInfoResult (Để truyền vào Prompt)
data class UserInfoResult(
    ... // giữ nguyên
    val viewingYear: Int,
    val viewingMonth: Int = 0,
    val viewingMode: String = "YEAR", // "YEAR" hoặc "MONTH"
    val readingStyle: String
)
```

---

## 2. Danh Sách Màn Hình & Thay Đổi UI

### Sửa đổi trên `InputScreen.kt`

Section **"Năm xem hạn"** sẽ được đổi tên thành **"Khoảng thời gian luận giải"**.

**Sơ đồ Layout (Mockup):**
```
┌─────────────────────────────────────────┐
│  📅 Khoảng thời gian luận giải          │
│                                         │
│  ○ Theo năm    ● Theo tháng             │
│                                         │
│  ┌──────────────┐ ┌──────────────────┐  │
│  │ Tháng 4   ▼  │ │ Năm 2026      ▼ │  │
│  └──────────────┘ └──────────────────┘  │
└─────────────────────────────────────────┘
```

**Chi tiết Logic UI:**
1. Có 2 RadioButton: `Theo năm` (chọn mặc định) và `Theo tháng`.
2. Khi chọn `Theo năm`: Chỉ hiển thị `YearSelector` (như cũ).
3. Khi chọn `Theo tháng`: Hiển thị `MonthSelector` (mới) bên trái và `YearSelector` bên phải.
   - Khi vừa chuyển sang `Theo tháng` lần đầu: Tự động set tháng = (tháng hiện tại hệ thống + 1). Nếu đang là tháng 12 thì set = 1 và năm = năm sau.

---

## 3. Luồng Hoạt Động (Prompt Engine)

### Yêu cầu: **Tất cả các "Phong cách luận giải" đều dùng chung cấu trúc Prompt 3**.

### Sửa đổi trong `GeminiClient.kt`

Hàm `constructPrompt()` sẽ được viết lại hoàn toàn để sử dụng cấu trúc chuyên sâu của Prompt 3. Giữ lại biến `selectedStylePrompt` để tinh chỉnh "giọng điệu" (tone of voice), nhưng **khung sườn luận giải (12 cung, tam hợp, xung chiếu, các cấm kỵ...) là dùng chung**.

**Cấu trúc Prompt mới (Pseudo-code):**

```kotlin
val vanHanRequest = if (info.viewingMode == "MONTH") {
    "Phân tích vận tháng ${info.viewingMonth} âm lịch năm ${info.viewingYear} (theo đại vận + tiểu vận + lưu thái tuế + lưu hóa tinh nếu có dữ liệu)"
} else {
    "Phân tích vận năm ${info.viewingYear} (theo đại vận hiện tại và lưu tinh năm)"
}

return """
Bạn là một nhà mệnh lý học chuyên sâu về TỬ VI ĐẨU SỐ, có khả năng phân tích tinh hệ ở mức cấu trúc – không luận theo cảm tính.

MỤC TIÊU:
Luận giải lá số dựa trên hệ thống sao – cung – vận – ngũ hành một cách logic, có dẫn chứng tinh hệ cụ thể cho từng nhận định.

PHONG CÁCH:
$selectedStylePrompt

==================================================
NGUYÊN TẮC BẮT BUỘC
==================================================
1. Phải xác định rõ: Mệnh đóng ở đâu, thuộc hành gì. Cục gì, sinh khắc giữa Mệnh và Cục...
2. Trong mỗi cung khi luận phải xét đầy đủ: Chính tinh, Phụ tinh trọng yếu, Tam hợp, Xung chiếu, Giáp cung, Tuần/Triệt, Hóa tinh.
3. Nếu xuất hiện cách cục đặc biệt phải chỉ rõ (Sát Phá Tham, Cơ Nguyệt Đồng Lương...)
4. Phải xác định đại vận và cung bị kích hoạt.
5. Khi luận vận: Xét đại vận, tiểu vận, lưu tinh.

==================================================
NHỮNG ĐIỀU KHÔNG ĐƯỢC LÀM
==================================================
- Không khẳng định tử vong, tai nạn nghiêm trọng, bệnh hiểm nghèo.
- Không đoán chính xác số lượng con cái. Không gán chỉ số IQ.
- Không suy diễn tâm linh dòng họ nếu tinh hệ không thể hiện rõ.
- Không nói “số đã định không thay đổi”.

==================================================
LÁ SỐ CỦA ĐƯƠNG SỐ:
1. Thông tin cơ bản:
[THÔNG TIN TỪ APP]

2. Các Cung và Sao:
[CHI TIẾT 12 CUNG CÓ SẴN]

==================================================
CẤU TRÚC LUẬN (Bắt buộc theo thứ tự này):
1. MỆNH (bắt buộc phân tích kỹ nhất, bao gồm Mệnh – Thân – Cục)
2. PHU THÊ
3. QUAN LỘC
4. TÀI BẠCH
5. THIÊN DI
6. TẬT ÁCH
7. ĐIỀN TRẠCH
8. PHÚC ĐỨC
9. PHỤ MẪU
10. HUYNH ĐỆ
11. NÔ BỘC
12. TỬ TỨC

Mỗi cung phải luận theo cấu trúc: 1. Chính tinh, 2. Phụ tinh, 3. Tam hợp-xung chiếu-giáp cung, 4. Tuần/Triệt, 5. Hóa tinh, 6. Nhận định tổng hợp.

==================================================
PHẦN TỔNG KẾT BẮT BUỘC
==================================================
- Tổng quan mệnh cách (ổn định / biến động / thành muộn / đa truân...)
- Điểm mạnh nổi bật nhất (dẫn chứng sao)
- Điểm dễ tự làm khó mình (dẫn chứng sao)
- Chiến lược tu dưỡng thực tế phù hợp mệnh cách
- $vanHanRequest
"""
```

---

## 4. Checklist Kiểm Tra (Acceptance Criteria)

### Tính năng: Xem vận hạn theo Năm / Tháng
- [ ] Chọn "Theo năm" thì giao diện chỉ hiện ô chọn Năm.
- [ ] Chọn "Theo tháng" thì giao diện hiện 2 ô: Tháng và Năm cạnh nhau.
- [ ] Khi lần đầu bấm qua "Theo tháng", ô Tháng tự nhảy tới (Tháng hiện tại + 1). (VD: Nay là tháng 3, nó tự nhảy tháng 4). Xử lý đúng nếu qua năm mới.
- [ ] Các thông tin này (viewingMonth, viewingMode) truyền đúng vào được hàm tạo prompt.

### Tính năng: Áp dụng Prompt Chuyên Sâu cho Mọi Style
- [ ] Chọn bất kỳ Style nào (Đời thường, Hài hước...) cũng sẽ tạo ra bài luận đủ 12 cung, có phân tích chi tiết.
- [ ] Giọng điệu bài luận vẫn phải phù hợp với Style đã chọn (Ví dụ: "Hài hước" thì văn phong vui vẻ nhưng nội dung vẫn là phân tích chuyên sâu).
- [ ] Phần Vận Hạn cuối bài đổi chữ chính xác theo mode (Tháng X năm Y, hoặc Năm Y).
- [ ] Nút "Copy Prompt" sinh ra đúng layout Prompt 3 đã thiết kế.

---
*Tạo bởi AWF 2.1 - Design Phase*
