# 🎨 DESIGN: Tối Ưu Hóa JSON Prompt v2.0

**Ngày tạo:** 2026-03-10
**Dựa trên:** Nhận xét chuyên gia (9.2/10) + Prompt2.txt hiện tại

---

## 1. Tổng Quan Thay Đổi

Chỉ sửa **1 file duy nhất**: `GeminiClient.kt`. Logic an sao (`TuViLogic.kt`, `Constants.kt`) **hoàn toàn không đổi**.

Mục tiêu: Biến các dữ liệu đang ở dạng **chuỗi văn xuôi** thành **JSON Object/Array** thực thụ, giúp AI parse chính xác hơn.

| # | Thay đổi | Vùng ảnh hưởng | Độ khó |
|---|----------|----------------|--------|
| 1 | `phi_tinh_tu_hoa`: string → object | `buildChartDataJson()` | Trung bình |
| 2 | `dai_van_list`: string → array | `buildChartDataJson()` | Thấp |
| 3 | `fixed_stars` / `transit_stars`: tách name, state, type | `buildPalacesJsonArray()` | Trung bình |
| 4 | Thêm `reasoning_rules` block | `constructPrompt()` | Thấp |

---

## 2. Thiết Kế Chi Tiết Từng Thay Đổi

### 2.1. `phi_tinh_tu_hoa` → JSON Object

#### TRƯỚC (string dài, AI phải đọc như người):
```json
"phi_tinh_tu_hoa": "Tý(Canh): H.Lộc→Thìn, H.Quyền→Mão, H.Khoa→Tuất, H.Kỵ→Dần\nSửu(Tân): H.Lộc→Tý..."
```

#### SAU (structured object, AI truy xuất trực tiếp):
```json
"phi_tinh_tu_hoa": {
  "Tý": {"can": "Canh", "loc": "Thìn", "quyen": "Mão", "khoa": "Tuất", "ky": "Dần"},
  "Sửu": {"can": "Tân", "loc": "Tý", "quyen": "Thìn", "khoa": "Thân", "ky": "Ngọ"}
}
```

#### Cách làm:
Thêm hàm `parsePhiTinhToJson(rawString: String): JSONObject` trong `GeminiClient.kt`:
```kotlin
private fun parsePhiTinhToJson(raw: String): JSONObject {
    val result = JSONObject()
    raw.trim().lines().filter { it.isNotBlank() }.forEach { line ->
        // Format: "Tý(Canh): H.Lộc→Thìn, H.Quyền→Mão, H.Khoa→Tuất, H.Kỵ→Dần"
        val match = Regex("""^(\S+)\((\S+)\):\s*(.+)$""").find(line.trim())
        if (match != null) {
            val (cung, can, rest) = match.destructured
            val obj = JSONObject().apply { put("can", can) }
            rest.split(",").map { it.trim() }.forEach { part ->
                val hoaMatch = Regex("""H\.(\S+)→(\S+)""").find(part)
                if (hoaMatch != null) {
                    val (hoaType, target) = hoaMatch.destructured
                    obj.put(hoaType.lowercase(), target) // loc, quyen, khoa, ky
                }
            }
            result.put(cung, obj)
        }
    }
    return result
}
```

Gọi tại `buildChartDataJson()`:
```diff
- put("phi_tinh_tu_hoa", info.phiTinhTuHoa.ifEmpty { "Không có..." })
+ put("phi_tinh_tu_hoa", if (info.phiTinhTuHoa.isNotEmpty())
+     parsePhiTinhToJson(info.phiTinhTuHoa)
+   else JSONObject().apply { put("status", "Không có dữ liệu phi tinh") })
```

---

### 2.2. `dai_van_list` → JSON Array

#### TRƯỚC:
```json
"dai_van_list": "5–14: Tân Sửu | 15–24: Canh Tý | 25–34: Kỷ Hợi | ..."
```

#### SAU:
```json
"dai_van_list": [
  {"age": "5-14", "can": "Tân", "cung": "Sửu"},
  {"age": "15-24", "can": "Canh", "cung": "Tý"},
  {"age": "25-34", "can": "Kỷ", "cung": "Hợi"}
]
```

#### Cách làm:
Thêm hàm `parseDaiVanToJson(rawString: String): JSONArray`:
```kotlin
private fun parseDaiVanToJson(raw: String): JSONArray {
    val result = JSONArray()
    raw.split("|").map { it.trim() }.filter { it.isNotBlank() }.forEach { entry ->
        // Format: "5–14: Tân Sửu"
        val match = Regex("""^(\d+[–-]\d+):\s*(\S+)\s+(\S+)$""").find(entry)
        if (match != null) {
            val (age, can, cung) = match.destructured
            result.put(JSONObject().apply {
                put("age", age)
                put("can", can)
                put("cung", cung)
            })
        }
    }
    return result
}
```

Gọi tại `buildChartDataJson()` → `metadata`:
```diff
- put("dai_van_list", info.daiVanFullList)
+ put("dai_van_list", parseDaiVanToJson(info.daiVanFullList))
```

---

### 2.3. `fixed_stars` & `transit_stars` → Tách Trạng Thái + Loại Sao

#### TRƯỚC:
```json
"fixed_stars": [
  "Thiên Tướng (M)",
  "(Hóa Lộc)",
  "Kình Dương (H)"
]
```

#### SAU:
```json
"fixed_stars": [
  {"name": "Thiên Tướng", "type": "main_star", "state": "M"},
  {"name": "Hóa Lộc", "type": "tu_hoa"},
  {"name": "Kình Dương", "type": "sat_tinh", "state": "H"}
]
```

#### Bảng phân loại `star_type`:

| Type | Mô tả (cho AI) | Danh sách sao |
|------|----------------|---------------|
| `main_star` | 14 Chính tinh | `Constants.CHINH_TINH` |
| `sat_tinh` | Sát tinh (hung) | Kình Dương, Đà La, Hỏa Tinh, Linh Tinh, Địa Không, Địa Kiếp, Thiên Hình |
| `cat_tinh` | Cát tinh (lành) | Văn Xương, Văn Khúc, Tả Phù, Hữu Bật, Thiên Khôi, Thiên Việt, Lộc Tồn, Thiên Mã, Đào Hoa, Hồng Loan, Long Trì, Phượng Các |
| `tu_hoa` | Tứ hóa bản mệnh/đại vận/lưu niên | (Hóa Lộc), (Hóa Quyền), (Hóa Khoa), (Hóa Kỵ) và ĐV./L. variants |
| `sub_star` | Phụ tinh khác | Tất cả sao còn lại |

#### Cách làm:
Thêm hàm helper và constants:
```kotlin
private val SAT_TINH = setOf("Kình Dương", "Đà La", "Hỏa Tinh", "Linh Tinh",
    "Địa Không", "Địa Kiếp", "Thiên Hình", "Kiếp Sát")

private val CAT_TINH = setOf("Văn Xương", "Văn Khúc", "Tả Phù", "Hữu Bật",
    "Thiên Khôi", "Thiên Việt", "Lộc Tồn", "Thiên Mã", "Đào Hoa",
    "Hồng Loan", "Thiên Hỷ", "Long Trì", "Phượng Các", "Thiên Đức",
    "Nguyệt Đức", "Ân Quang", "Thiên Quý", "Thiên Quan", "Thiên Phúc",
    "Quốc Ấn", "Đường Phù", "Thai Phụ", "Phong Cáo", "Tam Thai",
    "Bát Tọa", "Thiên Giải", "Địa Giải", "Giải Thần")

private fun parseStarToJson(raw: String, isChinhTinh: Boolean): JSONObject {
    val obj = JSONObject()
    var working = raw.trim()

    // Extract flags like [BỊ TRIỆT LỘ], [BỊ TUẦN KHÔNG]
    val flags = JSONArray()
    Regex("""\[([^\]]+)\]""").findAll(working).forEach { flags.put(it.groupValues[1]) }
    working = working.replace(Regex("""\s*\[[^\]]+\]"""), "")

    // Check if Tứ Hóa: "(Hóa Lộc)", "(ĐV. Hóa Kỵ)", "(L.Hóa Lộc)"
    if (working.startsWith("(") && working.endsWith(")")) {
        obj.put("name", working.removeSurrounding("(", ")"))
        obj.put("type", "tu_hoa")
        return obj
    }

    // Extract state: "Thiên Tướng (M)" → name="Thiên Tướng", state="M"
    val stateMatch = Regex("""^(.+?)\s*\(([MVĐHB]|Bình)\)$""").find(working)
    val name: String
    if (stateMatch != null) {
        name = stateMatch.groupValues[1].trim()
        obj.put("state", stateMatch.groupValues[2])
    } else {
        name = working
    }

    // Strip ĐV. / L. prefix for type classification
    val baseName = name.removePrefix("ĐV. ").removePrefix("L.")

    obj.put("name", name)
    obj.put("type", when {
        isChinhTinh -> "main_star"
        baseName in SAT_TINH -> "sat_tinh"
        baseName in CAT_TINH -> "cat_tinh"
        else -> "sub_star"
    })
    if (flags.length() > 0) obj.put("flags", flags)
    return obj
}
```

Cập nhật `buildPalacesJsonArray()`:
```diff
- put("fixed_stars", JSONArray(c.chinhTinh + fixedPhu))
+ val starArr = JSONArray()
+ c.chinhTinh.forEach { starArr.put(parseStarToJson(it, true)) }
+ fixedPhu.forEach { starArr.put(parseStarToJson(it, false)) }
+ put("fixed_stars", starArr)

- put("transit_stars", JSONArray(daiVanStars + luuStars))
+ val transitArr = JSONArray()
+ (daiVanStars + luuStars).forEach { transitArr.put(parseStarToJson(it, false)) }
+ put("transit_stars", transitArr)
```

---

### 2.4. Thêm `reasoning_rules` Block

#### Thiết kế:
```json
"reasoning_rules": {
  "always_show_evidence": true,
  "evidence_format": "(Căn cứ: sao + trạng thái + cung)",
  "minimum_evidence": 2,
  "conflict_resolution": "priority_rules"
}
```

#### Cách làm:
Thêm vào hàm `constructPrompt()` ngay sau `common_mistakes`:
```kotlin
json.put("reasoning_rules", JSONObject().apply {
    put("always_show_evidence", true)
    put("evidence_format", "(Căn cứ: sao + trạng thái + cung)")
    put("minimum_evidence", 2)
    put("conflict_resolution", "priority_rules")
})
```

---

## 3. Sơ Đồ Luồng Dữ Liệu (Trước vs Sau)

```
                         TRƯỚC                              SAU
                    ┌─────────────┐                   ┌─────────────┐
TuViLogic           │ String:     │                   │ String:     │
(KHÔNG ĐỔI)  ────→ │ "5-14: Tân  │             ────→ │ "5-14: Tân  │
                    │  Sửu | ..." │                   │  Sửu | ..." │
                    └──────┬──────┘                   └──────┬──────┘
                           │                                  │
                           │ GeminiClient                     │ GeminiClient
                           │ (copy nguyên)                    │ (PARSE → JSON)
                           ▼                                  ▼
                    ┌─────────────┐                   ┌─────────────┐
JSON Output         │ "dai_van":  │                   │ "dai_van":  │
                    │ "5-14: Tân  │                   │ [{age:"5-14"│
                    │  Sửu | ..." │                   │   can:"Tân" │
                    │  (STRING)   │                   │   cung:"Sửu"│
                    └─────────────┘                   │  }, ...]    │
                                                      │  (ARRAY)    │
                                                      └─────────────┘
```

**Điểm mấu chốt:** An toàn tuyệt đối vì TuViLogic vẫn xuất chuỗi → GeminiClient nhận chuỗi → parse tại chỗ → xuất JSON.

---

## 4. Kiểm Tra (Test Cases)

| # | Test | Điều kiện | Kỳ vọng |
|---|------|-----------|---------|
| TC-01 | JSON Valid | `constructPrompt()` | Output parse được, không crash |
| TC-02 | phi_tinh structured | Cung Tuất(Canh) | `phi_tinh_tu_hoa.Tuất.can == "Canh"` |
| TC-03 | dai_van array | 10 decades | `dai_van_list.length() == 10` |
| TC-04 | star has type | Cung Sửu (Mệnh) | `fixed_stars[0].type == "main_star"` |
| TC-05 | star has state | Thiên Tướng | `fixed_stars[0].state == "M"` |
| TC-06 | tu_hoa detected | (Hóa Lộc) | `type == "tu_hoa"` |
| TC-07 | sat_tinh detected | Kình Dương | `type == "sat_tinh"` |
| TC-08 | reasoning_rules | Always | Key exists in root JSON |
| TC-09 | Legacy tests | `./gradlew test` | All 11 existing tests PASS |

---

## 5. Files Thay Đổi

| File | Thay đổi | Rủi ro |
|------|----------|--------|
| `GeminiClient.kt` | +3 helper functions, sửa 2 builders | **Trung bình** |
| `DataLayerVerificationTest.kt` | Cập nhật assertions cho JSON keys mới | **Thấp** |
| `Level5DeepVerificationTest.kt` | Cập nhật assertions cho JSON keys mới | **Thấp** |
| `Constants.kt` | **Không đổi** | Không |
| `TuViLogic.kt` | **Không đổi** | Không |
| `Models.kt` | **Không đổi** | Không |

---

*Tạo bởi AWF - Design Phase (2026-03-10)*
