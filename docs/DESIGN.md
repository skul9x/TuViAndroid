# 🎨 DESIGN: Nâng cấp Dữ liệu TuVi Prompt (Level 5.1)

Ngày tạo: 2026-03-10
Dựa trên: Kế hoạch cải thiện tính chuyên gia của Prompt AI (Level 5.0).

---

## 1. Cách Lưu Thông Tin (Database & Logic)
Chức năng luận giải Tử Vi của app không dùng Database SQLite thông thường (như SQL table cho từng sao), mà dùng **Hardcoded Mappings trong Code** (Cụ thể là `Constants.kt` và logic trong `TuViLogic.kt`).  

Việc thiết kế lần này cần can thiệp vào cách dữ liệu được trích xuất trước khi "đút" cho Gemini.

### 1.1 Sơ Đồ Biến Đổi Dữ Liệu
```text
┌─────────────────────────────────────────────────────────────┐
│  🧩 RAW DATA (Dữ liệu ban đầu)                             │
│  ├── Cung hiện tại: Tỵ (Lệnh tháng 4)                       │
│  ├── Chính Tinh: Vũ Khúc, Phá Quân                          │
│  └── Phụ Tinh: Kình Dương, Địa Không, Văn Xương              │
└───────────────────────────┬─────────────────────────────────┘
                            │ Xử lý qua Logic (TuViLogic.kt)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  ⚙️ DATA ENHANCEMENT (Gắn thêm thuộc tính - TÍNH NĂNG MỚI)  │
│  ├── 1. Mapping độ sáng: Kình (Hãm), Không (Miếu)...         │
│  ├── 2. Mapping Tần/Triệt: Vũ Khúc (Bị Triệt lộ)             │
│  └── 3. Nhóm sao hội hợp: Tạo Cách Cục (Sát Phá Tham...)     │
└────────────────────────────────────┼────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────┐
│  📄 OUTPUT PROMPT (String cho AI)                           │
│  ├── Cung Tỵ [Hỏa] (Quan Lộc):                              │
│  │   + Cố định: Vũ Khúc (B) [Triệt], Phá Quân (H) [Triệt]   │
│  │   + Phụ tinh: Kình Dương (H), Bệnh, Địa Không (M)        │
│  │   + Lưu Niên: L.Kình Dương, L.Lộc Tồn                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Thiết Kế Logic & Quy Tắc An Sao (Implementation Details)

### 2.1 Bản đồ Độ Sáng Lục Sát, Lục Bại, Lục Cát (PHU_TINH_BRIGHTNESS)
Quy tắc Miếu (M) / Hãm (H) sẽ được lưu dưới dạng `Map<String, List<Int>>` (hoặc logic block) trong `Constants.kt`.

* **Tứ Mộ (Thìn=4, Tuất=10, Sửu=1, Mùi=7)**
* **Tứ Sinh (Dần=2, Thân=8, Tỵ=5, Hợi=11)**
* **Tứ Chính (Tý=0, Ngọ=6, Mão=3, Dậu=9)**

| Phụ Tinh | Vị trí Đắc/Miếu (M) | Vị trí Hãm (H) |
| :--- | :--- | :--- |
| **Kình Dương** | Tứ Mộ (Thìn, Tuất, Sửu, Mùi) | Tứ Chính (Tý, Ngọ, Mão, Dậu) |
| **Đà La** | Tứ Mộ (Một số phái thêm Tứ Sinh) | Tứ Sinh (Dần, Thân, Tỵ, Hợi) |
| **Địa Không** | Tứ Sinh (Dần, Thân, Tỵ, Hợi) | Các cung còn lại (Tứ Chính, Tứ Mộ) |
| **Địa Kiếp** | Tứ Sinh (Dần, Thân, Tỵ, Hợi) | Các cung còn lại (Tứ Chính, Tứ Mộ) |
| **Văn Xương** | Tỵ, Dậu, Sửu, Thân, Tý, Thìn | Dần, Ngọ, Tuất, Mão, Mùi, Hợi |
| **Văn Khúc** | Tỵ, Dậu, Sửu, Thân, Tý, Thìn | Dần, Ngọ, Tuất, Mão, Mùi, Hợi |

👉 *Cách code:* Viết hàm `fun getDoSangPhuTinh(saoName: String, cungIndex: Int): String` trả về `"(M)"` hoặc `"(H)"`.

### 2.2 Đảo Ngữ Tuần / Triệt Đối Với Chính Tinh
Khi một cung `contains("Triệt")` hoặc `contains("Tuần")`:
* **Hiện tại:** Ghi ở đầu cung `Cung Tý (Gặp Triệt): Tử Vi (Bình)...` (Rất dễ làm AI quên tính chất Triệt).
* **Thiết kế mới:** Phải dán nhãn trực tiếp vào sau Chính tinh.
  * Cũ: `Tử Vi (Bình)`
  * Mới: `Tử Vi (Bình) [BỊ TRIỆT LỘ]` hoặc `[BỊ TUẦN KHÔNG]`.

### 2.3 Cấu Trúc Lại Chuỗi Sinh Cung (GeminiClient.kt)
Tách dòng để mắt AI dễ bóc tách dữ liệu:
```kotlin
// Giả mã cấu trúc mới của Cung trong String Builder
val sbCung = StringBuilder()
sbCung.append("- Cung ${tenCung} [${nguHanh}] (${tenCungMoTa}):\n")
sbCung.append("  + Cố định: ${chinhTinhVaPhuTinh}\n")
sbCung.append("  + Vận Hạn: ${saoDaiVanVaLuuNien}\n")
```

---

## 3. Checklist Kiểm Tra (Acceptance Criteria)

### Tính năng: Nâng cấp Dữ liệu Cung Tử Vi
Mục đích: Cung cấp đầy đủ trạng thái sao để AI áp dụng Rule #4 hiệu quả (Phân biệt Miếu/Hãm).

- [ ] Bài Test 1: Các sao Kình, Đà, Không, Kiếp, Xương, Khúc phải xuất hiện với hậu tố `(M)` hoặc `(H)` tương ứng với vị trí cung của chúng.
- [ ] Bài Test 2: Nếu cung đó "Gặp Triệt", tất cả Chính Tinh trong cung đó phải xuất hiện kèm chuỗi `[BỊ TRIỆT]` ngay phía sau tên sao.
- [ ] Bài Test 3: Output của mỗi cung trong prompt phải được ngắt dòng rõ ràng giữa phần "Cố định" và "Đại vận/Lưu niên".
- [ ] Bài Test 4: Chạy Unit Test `TuViLogicTest` không bị Fail các kịch bản cũ.

---
*Tạo bởi AWF 2.1 - Design Phase*
