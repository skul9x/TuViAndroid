# 🎨 DESIGN: Chuyển Prompt Văn Xuôi → JSON

**Dựa trên:** implementation_plan.md

---

## 1. Kiến Trúc Code (Cách Các Phần Kết Nối)

```
GeminiClient.kt
├── constructPrompt(data: LasoData): String    ← HÀM CHÍNH (sẽ sửa)
│   │
│   ├── buildStyleJson(data)                   ← [MỚI] Tone luận giải
│   ├── buildAbsoluteRulesJson(data)           ← [MỚI] 8-9 nguyên tắc (incl. rule9 trẻ em)
│   ├── buildPipelineJson()                    ← [MỚI] 4 bước phân tích + ranking
│   ├── buildMethodsJson()                     ← [MỚI] 7 phương pháp luận
│   ├── buildPalaceMethodJson()                ← [MỚI] 5 bước + trọng số tương tác
│   ├── buildOutputFormatJson(data)            ← [MỚI] Format A-E1 (viewingYear dynamic)
│   ├── buildNotationAndMistakesJson()         ← [MỚI] Ký hiệu + lỗi phổ biến
│   ├── buildChartDataJson(data)               ← [MỚI] Toàn bộ data lá số
│   │   ├── buildPersonJson(info)
│   │   ├── buildMetadataJson(info, cungList)
│   │   ├── buildPalacesJsonArray(cungList)    ← 12 cung → JSONArray
│   │   ├── buildTuHoaSummaryJson(cungList)    ← Tứ hóa 3 tầng → JSONObject
│   │   └── buildPhiTinhJson(info)             ← Phi tinh → String (giữ nguyên)
│   │
│   └── return json.toString(2)                ← Pretty-print 2 spaces
│
├── constructPromptLegacy(data)                ← [GIỮ LẠI] Backup hàm cũ
├── buildTuHoaSummary(cungList)                ← [GIỮ NGUYÊN] Helper cũ (dùng cho legacy)
└── detectBoSao(cungList)                      ← [GIỮ NGUYÊN] Logic phát hiện bộ sao
```

**Nguyên tắc:** Dùng `org.json.JSONObject` + `org.json.JSONArray` (có sẵn trong Android SDK, **không cần thêm dependency**).

---

## 2. Luồng Dữ Liệu (Data Flow)

```
TuViLogic.anSao(input)
    │
    ▼
LasoData { info: UserInfoResult, cung: List<CungInfo>, scores }
    │
    ▼
GeminiClient.constructPrompt(data)
    │
    ├── info.readingStyle ────────────→ "style.tone"
    ├── info (name, gender, date) ───→ "chart_data.person"
    ├── info (menhNguHanh, amDuong) ─→ "chart_data.metadata"
    ├── info.phiTinhTuHoa ───────────→ "chart_data.phi_tinh" (giữ nguyên string)
    ├── info (viewingYear, daiVan) ──→ "chart_data.fortune_request"
    ├── cungList ────────────────────→ "chart_data.palaces" (12 objects)
    │   └── Phân tách: fixed_stars vs transit_stars (ĐV.*/L.*)
    ├── detectBoSao(cungList) ───────→ "chart_data.metadata.nhom_sao"
    ├── buildTuHoaSummaryJson() ─────→ "chart_data.tu_hoa_summary"
    └── Constants.* ────────────────→ Static JSON keys (rules, methods, configs)
```

---

## 3. Thiết Kế JSON Cho Từng Cung (Palace)

Đây là phần **quan trọng nhất** vì chiếm phần lớn dữ liệu:

### Trước (văn xuôi):
```
- Cung Dần [Mộc] (Phụ Mẫu):
  + Cố định: Thiên Đồng (M), Thiên Lương (V), Tang Môn (Đ)...
  + Vận Hạn: (ĐV. Hóa Khoa), L.Bạch Hổ (Đ), (L.Hóa Lộc)
```

### Sau (JSON):
```json
{
  "name": "Dần",
  "element": "Mộc",
  "function": "Phụ Mẫu",
  "vo_chinh_dieu": false,
  "flags": [],
  "fixed_stars": [
    "Thiên Đồng (M)", "Thiên Lương (V)", "Tang Môn (Đ)",
    "Tiểu Hao (Đ)", "Bệnh", "Thiên Mã (V)", "(Hóa Lộc)",
    "Linh Tinh (M)", "Cô Thần", "Bát Tọa"
  ],
  "transit_stars": [
    "(ĐV. Hóa Khoa)", "L.Bạch Hổ (Đ)", "(L.Hóa Lộc)"
  ]
}
```

### Logic phân tách (giữ nguyên từ code hiện tại):
```kotlin
// Fixed = tất cả sao KHÔNG bắt đầu bằng ĐV. hoặc L.
val fixedPhu = c.phuTinh.filter { 
    !it.startsWith("ĐV.") && !it.startsWith("L.") && 
    !it.startsWith("(ĐV.") && !it.startsWith("(L.") 
}
// Transit = ĐV.* + L.*
val daiVanStars = c.phuTinh.filter { it.startsWith("ĐV.") || it.startsWith("(ĐV.") }
val luuStars = c.phuTinh.filter { it.startsWith("L.") || it.startsWith("(L.") }
```

### Flags:
```kotlin
val flags = mutableListOf<String>()
if (c.chinhTinh.isEmpty()) flags.add("Vô chính diệu")
if (c.phuTinh.any { it.startsWith("Tuần") }) flags.add("Gặp Tuần")
if (c.phuTinh.any { it.startsWith("Triệt") }) flags.add("Gặp Triệt")
```

---

## 4. Quyết Định Thiết Kế: Phi Tinh

`info.phiTinhTuHoa` hiện là **1 string dài**. Quyết định: **Giữ nguyên string** vì:
- AI chỉ cần đọc, không truy xuất theo key
- Parse sang JSON tốn thêm ~30% token mà không gain accuracy
- Giữ consistency với cách data được compute

---

## 5. Bảng Tra Tứ Hóa 10 Can → JSON Object

```json
"tu_hoa_10_can": {
  "_warning": "CHỈ dùng GIẢI THÍCH cơ chế phi tinh. KHÔNG dùng để tự tính thêm",
  "Giáp": {"Lộc":"Liêm Trinh", "Quyền":"Phá Quân", "Khoa":"Vũ Khúc", "Kỵ":"Thái Dương"},
  "Ất": {"Lộc":"Thiên Cơ", "Quyền":"Thiên Lương", "Khoa":"Tử Vi", "Kỵ":"Thái Âm"},
  "Bính": {"Lộc":"Thiên Đồng", "Quyền":"Thiên Cơ", "Khoa":"Văn Xương", "Kỵ":"Liêm Trinh"},
  "Đinh": {"Lộc":"Thái Âm", "Quyền":"Thiên Đồng", "Khoa":"Thiên Cơ", "Kỵ":"Cự Môn"},
  "Mậu": {"Lộc":"Tham Lang", "Quyền":"Thái Âm", "Khoa":"Hữu Bật", "Kỵ":"Thiên Cơ"},
  "Kỷ": {"Lộc":"Vũ Khúc", "Quyền":"Tham Lang", "Khoa":"Thiên Lương", "Kỵ":"Văn Khúc"},
  "Canh": {"Lộc":"Thái Dương", "Quyền":"Vũ Khúc", "Khoa":"Thái Âm", "Kỵ":"Thiên Đồng"},
  "Tân": {"Lộc":"Cự Môn", "Quyền":"Thái Dương", "Khoa":"Văn Khúc", "Kỵ":"Văn Xương"},
  "Nhâm": {"Lộc":"Thiên Lương", "Quyền":"Tử Vi", "Khoa":"Tả Phù", "Kỵ":"Vũ Khúc"},
  "Quý": {"Lộc":"Phá Quân", "Quyền":"Cự Môn", "Khoa":"Thái Âm", "Kỵ":"Tham Lang"}
}
```

---

## 6. Cách Cục Mở Rộng (từ Constants.kt)

```json
"major_configurations": {
  "check_list": [
    "Tử Phủ Vũ Tướng", "Phủ Tướng Triều Viên", "Cơ Nguyệt Đồng Lương",
    "Nhật Nguyệt Tịnh Minh", "Sát Phá Tham", "Liêm Tham",
    "Cự Nhật", "Vũ Khúc tài tinh", "Thiên Phủ tài khố", "Thái Âm tài tinh"
  ],
  "extended": {
    "dai_quy": [ "...từ Constants.CACH_CUC_DAI_QUY..." ],
    "dai_phu": [ "...từ Constants.CACH_CUC_DAI_PHU..." ],
    "vo": [ "...từ Constants.CACH_CUC_VO..." ],
    "hung_pha": [ "...từ Constants.CACH_CUC_HUNG..." ],
    "dac_biet": [ "...từ Constants.CACH_CUC_DAC_BIET..." ]
  },
  "validation_warning": "Nhóm sao hội hợp CHỈ là gợi ý. AI phải TỰ XÁC ĐỊNH bằng: độ sáng, Tuần/Triệt, Tứ Hóa"
}
```

---

## 7. Test Cases

| # | Test | Điều kiện | Kỳ vọng |
|---|------|-----------|---------|
| TC-01 | JSON Valid | Gọi `constructPrompt()` | Output parse được thành JSONObject |
| TC-02 | Content Coverage | So sánh JSON vs prompt cũ | 100% rules, methods, data đều có |
| TC-03 | Palace Separation | Cung có sao ĐV. và L. | `fixed_stars` ≠ `transit_stars` |
| TC-04 | Rule9 (trẻ em) | Đương số < 13 tuổi | JSON có `child_rule` |
| TC-05 | Unit Tests | `./gradlew test` | Tất cả PASS |

---

## 8. Files Thay Đổi

| File | Thay đổi | Rủi ro |
|------|----------|--------|
| `GeminiClient.kt` | Sửa `constructPrompt()`, thêm ~10 helper | **Trung bình** |
| `Constants.kt` | **Không đổi** | Không |
| `TuViLogic.kt` | **Không đổi** | Không |
| `Models.kt` | **Không đổi** | Không |

**Chỉ sửa 1 file duy nhất** (`GeminiClient.kt`). Logic tính toán lá số hoàn toàn không bị ảnh hưởng.
