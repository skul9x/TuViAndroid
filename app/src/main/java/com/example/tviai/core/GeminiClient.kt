package com.example.tviai.core

import com.example.tviai.data.LasoData
import com.example.tviai.data.CungInfo
import com.example.tviai.data.ReadingStyle
import com.example.tviai.data.SettingsDataStore
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import org.json.JSONArray

class GeminiClient(
    private var apiKeys: List<String>,
    private var modelName: String = "gemini-3-flash-preview",
    private val settingsDataStore: SettingsDataStore? = null
) {
    
    companion object {
        /**
         * Model priority list: Best reasoning → Fastest fallback
         * "Hết nạc mới vạc tới xương" - Flash models only
         */
        val MODEL_PRIORITY = listOf(
            "gemini-3-flash-preview",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-flash-latest",
            "gemini-flash-lite-latest"
        )

        /**
         * Test connection with a specific model
         */
        suspend fun testConnection(apiKey: String, modelName: String): Result<String> {
            return try {
                val model = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                )
                val response = model.generateContent("Xin chào")
                if (response.text != null) {
                    Result.success("Kết nối thành công với $modelName!")
                } else {
                    Result.failure(Exception("Không nhận được phản hồi từ Google."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Detect if an exception is a quota/rate limit error
         */
        fun isQuotaError(exception: Exception): Boolean {
            val msg = exception.message?.lowercase() ?: ""
            return msg.contains("429") ||
                   msg.contains("quota") ||
                   msg.contains("rate limit") ||
                   msg.contains("resource exhausted") ||
                   msg.contains("too many requests")
        }
    }

    // Secondary constructor for single API key (backward compatibility)
    constructor(apiKey: String, modelName: String = "gemini-3-flash-preview") : this(
        apiKeys = if (apiKey.isNotBlank()) listOf(apiKey) else emptyList(),
        modelName = modelName
    )

    fun updateConfig(apiKeys: List<String>, modelName: String) {
        this.apiKeys = apiKeys
        this.modelName = modelName
    }

    fun updateConfig(apiKey: String, modelName: String) {
        this.apiKeys = if (apiKey.isNotBlank()) listOf(apiKey) else emptyList()
        this.modelName = modelName
    }

    /**
     * Get the prompt for copying to clipboard (for user to paste elsewhere)
     */
    fun getPromptForCopy(data: LasoData): String {
        return constructPrompt(data)
    }

    /**
     * Get the currently configured model name
     */
    fun getModelName(): String = modelName

    /**
     * Generate AI reading with smart fallback:
     * 1. Try all models (priority order) with current API key
     * 2. If all models fail, rotate to next API key and retry
     * 3. If all keys and models exhausted, show "Hết Quota API"
     */
    fun generateReadingStream(data: LasoData): Flow<String> = flow {
        if (apiKeys.isEmpty() || apiKeys.all { it.isBlank() }) {
            emit("❌ Lỗi: Chưa có API Key. Vui lòng vào Cài đặt để nhập API Key.")
            return@flow
        }

        val prompt = constructPrompt(data)
        
        // Build the model try order: selected model first, then priority list
        val modelsToTry = mutableListOf(modelName)
        modelsToTry.addAll(MODEL_PRIORITY.filter { it != modelName })

        var success = false
        var lastError: Exception? = null
        var keyIndex = 0

        // Outer loop: API Keys
        while (keyIndex < apiKeys.size && !success) {
            val currentKey = apiKeys[keyIndex]
            
            // Silent switch - no message needed

            // Inner loop: Models (priority order)
            for ((modelIndex, modelToUse) in modelsToTry.withIndex()) {
                try {
                    val model = GenerativeModel(
                        modelName = modelToUse,
                        apiKey = currentKey
                    )

                    // Silent switch - no message needed (fallback is invisible to user)

                    val responseFlow: Flow<GenerateContentResponse> = model.generateContentStream(prompt)
                    
                    responseFlow.collect { chunk ->
                        chunk.text?.let { emit(it) }
                    }
                    
                    success = true
                    break // Success, exit model loop
                    
                } catch (e: Exception) {
                    lastError = e
                    
                    if (isQuotaError(e)) {
                        // Quota error: Try next model
                        continue
                    } else {
                        // Other error (e.g., model not found): Also try next
                        continue
                    }
                }
            }

            if (!success) {
                // All models failed for this key, try next key
                keyIndex++
                
                // Also notify SettingsDataStore to rotate (if available)
                settingsDataStore?.let {
                    try {
                        // This is a suspend function, but we're in a flow, so it's fine
                        // Actually, we can't call suspend directly here without runBlocking
                        // So we'll handle rotation in ViewModel instead
                    } catch (_: Exception) {}
                }
            }
        }

        if (!success) {
            emit("\n\n❌ **Hết Quota API**\n\nĐã thử tất cả API Keys và Models nhưng không thành công.\nVui lòng thêm API Key mới trong Cài đặt.\n\nLỗi cuối: ${lastError?.message ?: "Không xác định"}")
        }
    }

    private fun constructPrompt(data: LasoData): String {
        val info = data.info
        val cungList = data.cung
        val style = ReadingStyle.fromString(info.readingStyle)

        val stylePrompts = mapOf(
            ReadingStyle.NGHIEM_TUC to "Điềm đạm – phân tích mệnh lý – không văn hoa. Xưng hô: 'Tại hạ' hoặc 'Tôi', gọi người xem là 'Đương số'.",
            ReadingStyle.DOI_THUONG to "Đời thường – dân dã – dễ hiểu. Xưng hô: 'Tôi', gọi người xem là 'Bạn'.",
            ReadingStyle.HAI_HUOC to "Hài hước – trẻ trung – vui nhộn. Xưng hô: 'Ad' hoặc 'Tui', gọi người xem là 'Bồ'.",
            ReadingStyle.KIEM_HIEP to "Kiếm hiệp – cổ trang – văn phong phim chưởng. Xưng hô: 'Bần đạo' hoặc 'Lão phu', gọi người xem là 'Thí chủ'.",
            ReadingStyle.CHUA_LANH to "Nhẹ nhàng – chữa lành (healing) – khích lệ tinh thần. Xưng hô: 'Mình', gọi người xem là 'Bạn'.",
            ReadingStyle.CHUYEN_GIA to "Điềm đạm – chuyên sâu – phân tích mệnh lý ở mức cấu trúc cao nhất. Xưng hô: 'Tôi', gọi người xem là 'Đương số'."
        )
        val selectedStylePrompt = stylePrompts[style] ?: stylePrompts[ReadingStyle.NGHIEM_TUC]!!

        val json = JSONObject()
        json.put("role", "AI chuyên luận Tử Vi Đẩu Số theo hệ thống tinh hệ cổ điển")
        json.put("style", JSONObject().apply {
            put("tone", selectedStylePrompt)
        })
        json.put("methodology_sources", JSONArray(listOf("Thiên Lương", "Vân Đằng Thái Thứ Lang", "Tử Vi Đẩu Số Toàn Thư")))
        json.put("objective", "Phân tích lá số theo cấu trúc tinh hệ – không suy đoán cảm tính")
        json.put("absolute_rules", buildAbsoluteRulesJson(data))
        json.put("priority_rules", JSONArray(listOf(
            "Chính tinh > Phụ tinh (chính tinh quyết định bản chất cung)",
            "Miếu/Vượng > Đắc > Bình > Hãm (trạng thái quyết định lực — PHẢI dùng ký hiệu M/V/Đ/Bình/H có sẵn và [flags] có sẵn, KHÔNG tự đánh giá sáng/tối)",
            "Tứ hóa bản mệnh > Tứ hóa đại vận > Tứ hóa lưu niên",
            "Đồng cung > Tam hợp > Xung chiếu > Giáp cung",
            "Cách cục lớn > Tiểu cách (cách lớn chi phối toàn cục)"
        )))
        // axis_mapping chỉ nằm trong chart_data.metadata (tránh trùng lặp)
        json.put("analysis_pipeline", buildPipelineJson())
        json.put("analysis_methods", buildMethodsJson())
        json.put("palace_analysis_method", buildPalaceMethodJson())
        json.put("analysis_order", JSONArray(listOf(
            "Mệnh (phân tích kỹ nhất, bao gồm Mệnh–Thân–Cục)",
            "Phu Thê", "Quan Lộc", "Tài Bạch", "Thiên Di", "Tật Ách",
            "Điền Trạch", "Phúc Đức", "Phụ Mẫu", "Huynh Đệ", "Nô Bộc", "Tử Tức"
        )))
        json.put("output_format", buildOutputFormatJson(data))
        json.put("notation_rules", buildNotationJson())
        json.put("common_mistakes", buildMistakesJson())
        json.put("reasoning_rules", JSONObject().apply {
            put("always_show_evidence", true)
            put("evidence_format", "(Căn cứ: sao + trạng thái + cung + [quan hệ: đồng cung/tam hợp/xung chiếu nếu có] + [tứ hóa/Tuần-Triệt nếu có])")
            put("minimum_evidence", 2)
            put("conflict_resolution", "priority_rules")
        })
        json.put("chart_data", buildChartDataJson(data))

        return json.toString(2)
    }

    private fun constructPromptLegacy(data: LasoData): String {
        val info = data.info
        val cungList = data.cung
        val style = ReadingStyle.fromString(info.readingStyle)
        
        val stylePrompts = mapOf(
            ReadingStyle.NGHIEM_TUC to "Điềm đạm – phân tích mệnh lý – không văn hoa. Xưng hô: 'Tại hạ' hoặc 'Tôi', gọi người xem là 'Đương số'.",
            ReadingStyle.DOI_THUONG to "Đời thường – dân dã – dễ hiểu. Xưng hô: 'Tôi', gọi người xem là 'Bạn'.",
            ReadingStyle.HAI_HUOC to "Hài hước – trẻ trung – vui nhộn. Xưng hô: 'Ad' hoặc 'Tui', gọi người xem là 'Bồ'.",
            ReadingStyle.KIEM_HIEP to "Kiếm hiệp – cổ trang – văn phong phim chưởng. Xưng hô: 'Bần đạo' hoặc 'Lão phu', gọi người xem là 'Thí chủ'.",
            ReadingStyle.CHUA_LANH to "Nhẹ nhàng – chữa lành (healing) – khích lệ tinh thần. Xưng hô: 'Mình', gọi người xem là 'Bạn'.",
            ReadingStyle.CHUYEN_GIA to "Điềm đạm – chuyên sâu – phân tích mệnh lý ở mức cấu trúc cao nhất. Xưng hô: 'Tôi', gọi người xem là 'Đương số'."
        )
        
        val selectedStylePrompt = stylePrompts[style] ?: stylePrompts[ReadingStyle.NGHIEM_TUC]!!

        val cungDetails = cungList.joinToString("\n") { c ->
            val voChinhDieu = if (c.chinhTinh.isEmpty()) " [Vô chính diệu]" else ""
            val specialContext = StringBuilder()
            if (c.phuTinh.any { it.startsWith("Tuần") }) specialContext.append(" (Gặp Tuần)")
            if (c.phuTinh.any { it.startsWith("Triệt") }) specialContext.append(" (Gặp Triệt)")
            
            // Separate stars
            val fixedPhu = c.phuTinh.filter { 
                !it.startsWith("ĐV.") && !it.startsWith("L.") && 
                !it.startsWith("(ĐV.") && !it.startsWith("(L.") 
            }
            val daiVanStars = c.phuTinh.filter { it.startsWith("ĐV.") || it.startsWith("(ĐV.") }
            val luuStars = c.phuTinh.filter { it.startsWith("L.") || it.startsWith("(L.") }
            
            val sb = StringBuilder()
            sb.append("- Cung ${c.name} [${c.nguHanhCung}] (${c.chucNang})$voChinhDieu$specialContext:\n")
            sb.append("  + Cố định: ${(c.chinhTinh + fixedPhu).joinToString(", ")}\n")
            if (daiVanStars.isNotEmpty() || luuStars.isNotEmpty()) {
                val transits = (daiVanStars + luuStars).joinToString(", ")
                sb.append("  + Vận Hạn: $transits")
            }
            
            sb.toString()
        }

        // Build ngũ hành sao annotation for chính tinh
        val nguHanhSaoStr = Constants.NGU_HANH_SAO.entries.joinToString(", ") { (sao, hanh) ->
            "$sao=$hanh"
        }

        // Build 10-Can Transformation Table for AI reference
        val canTuHoaTable = Constants.THIEN_CAN.mapIndexed { index, can ->
            val hoa = Constants.TU_HOA_MAP[index] ?: listOf("", "", "", "")
            "$can: Lộc→${hoa[0]}, Quyền→${hoa[1]}, Khoa→${hoa[2]}, Kỵ→${hoa[3]}"
        }.joinToString("\n        ")

        // Build Can Chi 12 cung string
        val canChi12CungStr = cungList.joinToString(", ") { "${it.name}=${it.canChi}" }

        // Detect bộ sao đã hình thành
        val boSaoList = detectBoSao(cungList)

        // Build trục cung info
        val menhCung = cungList.find { it.chucNang.contains("Mệnh") }
        val trucCungStr = if (menhCung != null) {
            val menhIdx = menhCung.index
            val thienDiIdx = (menhIdx + 6) % 12
            val taiIdx = cungList.find { it.chucNang.contains("Tài Bạch") }?.index ?: -1
            val quanIdx = cungList.find { it.chucNang.contains("Quan Lộc") }?.index ?: -1
            val phucIdx = cungList.find { it.chucNang.contains("Phúc Đức") }?.index ?: -1
            val phuTheIdx = cungList.find { it.chucNang.contains("Phu Thê") }?.index ?: -1
            val dienIdx = cungList.find { it.chucNang.contains("Điền Trạch") }?.index ?: -1

            buildString {
                append("Trục Mệnh–Thiên Di: ${Constants.DIA_CHI[menhIdx]}–${Constants.DIA_CHI[thienDiIdx]}")
                if (taiIdx >= 0 && quanIdx >= 0) append(" | Tam hợp Mệnh–Tài–Quan: ${Constants.DIA_CHI[menhIdx]}–${Constants.DIA_CHI[taiIdx]}–${Constants.DIA_CHI[quanIdx]}")
                if (phucIdx >= 0 && taiIdx >= 0 && quanIdx >= 0) append(" | Trục Phúc–Tài–Quan: ${Constants.DIA_CHI[phucIdx]}–${Constants.DIA_CHI[taiIdx]}–${Constants.DIA_CHI[quanIdx]}")
                if (phuTheIdx >= 0 && taiIdx >= 0) append(" | Trục Phu Thê–Tài Bạch: ${Constants.DIA_CHI[phuTheIdx]}–${Constants.DIA_CHI[taiIdx]}")
                if (dienIdx >= 0 && phucIdx >= 0) append(" | Trục Điền–Phúc: ${Constants.DIA_CHI[dienIdx]}–${Constants.DIA_CHI[phucIdx]}")
            }
        } else ""

        val vanHanRequest = if (info.viewingMode == "MONTH") {
            "Phân tích vận tháng ${info.viewingMonth} âm lịch năm ${info.viewingYear}. " +
            "Sử dụng: đại vận + tiểu hạn + lưu niên tứ hóa đã cung cấp. " +
            "LƯU Ý: Dữ liệu hiện tại là cấp NĂM, chưa có lưu nguyệt tứ hóa. " +
            "Nếu không đủ căn cứ cho kết luận cấp tháng → nêu rõ giới hạn và luận ở mức năm."
        } else {
            "Phân tích vận năm ${info.viewingYear} (theo đại vận hiện tại và lưu tinh năm)"
        }
        
        val birthYear = info.solarDate.split("/").last().toIntOrNull() ?: info.viewingYear
        val approxAge = info.viewingYear - birthYear + 1
        val isChild = approxAge < 13 || info.daiVanInfo.contains("Chưa vào đại vận")

        val rule9 = if (isChild) {
            """
            
            9. 👶 LUẬN LÁ SỐ TRẺ EM (Đương số dưới 13 tuổi hoặc chưa vào đại vận):
            • TUYỆT ĐỐI KHÔNG phân tích sâu về Tiền Bạc (Tài bạch), Sự Nghiệp (Quan lộc), Tình Duyên (Phu thê).
            • CHỈ tập trung luận đoán về: Sức khỏe, tính cách bẩm sinh, khả năng tiếp thu/học tập, và môi trường cha mẹ nuôi dưỡng.
            • Phải dùng giọng văn phù hợp để tư vấn cho "Phụ huynh" của đương số (ví dụ: "Bé có xu hướng...", "Cha mẹ nên...").
            """.trimIndent()
        } else ""

        val lasoContent = """
        0. METADATA LÁ SỐ:
        - Mệnh Ngũ Hành (Nạp Âm): ${info.menhNguHanh}
        - Âm/Dương mệnh: ${info.amDuong}
        - Cục: ${info.cuc}
        - Quan hệ Mệnh – Cục: ${info.cucMenhRelation}
        - Ngũ hành 14 chính tinh: $nguHanhSaoStr
        - Nhóm sao hội hợp: ${if (boSaoList.isEmpty()) "Không phát hiện" else boSaoList.joinToString("; ")}
        - Trục cung: $trucCungStr
        - Can Chi 12 cung: $canChi12CungStr
        - Cung Tiểu Hạn năm ${info.viewingYear}: ${info.tieuHanCung}
        - Danh sách Đại Vận: ${info.daiVanFullList}
        - Phi Tinh Tứ Hóa (Pre-computed):
        ${info.phiTinhTuHoa.ifEmpty { "Không có dữ liệu phi tinh" }}

        TÓM TẮT TỨ HÓA (ĐỌC TRƯỚC KHI LUẬN):
        ${buildTuHoaSummary(cungList)}
        
        BẢNG TRA TỨ HÓA 10 CAN (DÙNG CHO PHI TINH):
        $canTuHoaTable
        ⚠️ Bảng tra Tứ Hóa 10 Can CHỈ dùng để GIẢI THÍCH cơ chế của dữ liệu Phi Tinh Tứ Hóa đã pre-compute.
        KHÔNG dùng bảng này để tự an sao, tự tính thêm tứ hóa hoặc suy ra dữ liệu chưa được cung cấp.

        1. THÔNG TIN CƠ BẢN:
        - Đương số: ${info.name} (${info.gender})
        - Ngày sinh (Dương lịch): ${info.solarDate} lúc ${info.time}
        - Ngày sinh (Âm lịch): ${info.lunarDate} (${info.canChi})
        - Cục: ${info.cuc}
        - Mệnh đóng tại: ${info.menhTai}
        - Thân đóng tại: ${info.thanTai}
        - Đại vận hiện tại: ${info.daiVanInfo}
        - Khoảng thời gian xem vận: ${if (info.viewingMode == "MONTH") "Tháng ${info.viewingMonth} năm ${info.viewingYear}" else "Năm ${info.viewingYear}"}

        2. CÁC CUNG VÀ SAO:
        $cungDetails

        3. YÊU CẦU BỔ SUNG VỀ VẬN HẠN:
        - $vanHanRequest
        """.trimIndent()

        return """
        PHONG CÁCH LUẬN: $selectedStylePrompt

        Bạn là một AI chuyên luận TỬ VI ĐẨU SỐ theo hệ thống tinh hệ cổ điển.
        Phương pháp luận dựa trên logic của các tài liệu kinh điển như:
        Thiên Lương – Vân Đằng Thái Thứ Lang – Tử Vi Đẩu Số Toàn Thư.

        Mục tiêu:
        Phân tích lá số theo cấu trúc tinh hệ – không suy đoán cảm tính.

        =====================================
        NGUYÊN TẮC TUYỆT ĐỐI

        1. Mọi nhận định BẮT BUỘC phải có căn cứ sao.

        2. Không dùng các câu chung chung kiểu:
        "số giàu", "số khổ", "số tốt"
        nếu không chỉ rõ tinh hệ và cơ chế.

        3. Không suy đoán khi thiếu dữ liệu.
        Thiếu thông tin → nêu rõ 'Không có trong dữ liệu được cung cấp' và bỏ qua phần không đủ căn cứ.

        4. Phân biệt rõ:

        Chính tinh
        Phụ tinh
        Cát tinh
        Sát tinh
        Tứ hóa
        Sao lưu
        Sao đại vận

        5. Không bỏ qua các tương tác tinh hệ:

        đồng cung
        tam hợp
        xung chiếu
        hội chiếu
        giáp cung

        6. Không thần bí hóa sát tinh.

        Sát tinh phải phân tích theo cơ chế:

        sát tinh + cát tinh
        sát tinh + vận
        sát tinh phá cách hay tạo đột phá

        7. Không khẳng định tuyệt đối.

        Phải dùng ngôn ngữ xác suất:

        "thường"
        "có xu hướng"
        "nếu vận hỗ trợ"

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
        $rule9

        =====================================
        QUY TẮC ƯU TIÊN KHI TÍN HIỆU MÂU THUẪN:
        1. Chính tinh > Phụ tinh (chính tinh quyết định bản chất cung)
        2. Miếu/Vượng > Đắc > Bình > Hãm (sáng quyết định lực)
        3. Tứ hóa bản mệnh > Tứ hóa đại vận > Tứ hóa lưu niên
        4. Đồng cung > Tam hợp > Xung chiếu > Giáp cung
        5. Cách cục lớn > Tiểu cách (cách lớn chi phối toàn cục)

        =====================================
        QUY TRÌNH PHÂN TÍCH BẮT BUỘC

        Trước khi luận chi tiết phải thực hiện 4 bước.

        -------------------------------------
        BƯỚC 1 – TÓM TẮT CẤU TRÚC LÁ SỐ

        Liệt kê:

        • chính tinh từng cung
        • tứ hóa
        • cung Mệnh
        • cung Thân
        • tam hợp Mệnh – Tài – Quan

        -------------------------------------
        BƯỚC 2 – ĐÁNH GIÁ LỰC LÁ SỐ

        Xác định:

        • Mệnh mạnh hay yếu
        • Thân cư cung nào
        • cục sinh hay khắc mệnh
        • có sát tinh nặng hay không
        • có cát tinh nâng đỡ không

        -------------------------------------
        BƯỚC 3 – KIỂM TRA CÁCH CỤC

        Rà soát các cách lớn:

        Tử Phủ Vũ Tướng
        Phủ Tướng triều viên
        Cơ Nguyệt Đồng Lương
        Nhật Nguyệt tịnh minh
        Sát Phá Tham
        Liêm Tham
        Cự Nhật
        Vũ Khúc tài tinh
        Thiên Phủ tài khố
        Thái Âm tài tinh
        
        CÁC CÁCH CỤC MỞ RỘNG (KÈM DỰ LIỆU):
        • ĐẠI QUÝ: ${Constants.CACH_CUC_DAI_QUY.joinToString(", ")}
        • ĐẠI PHÚ: ${Constants.CACH_CUC_DAI_PHU.joinToString(", ")}
        • CÁCH VÕ: ${Constants.CACH_CUC_VO.joinToString(", ")}
        • CÁCH HUNG/PHÁ: ${Constants.CACH_CUC_HUNG.joinToString(", ")}
        • CÁC BỘ SAO KHÁC: ${Constants.CACH_CUC_DAC_BIET.joinToString(", ")}

        • có sát tinh phá cách không

        ⚠️ LƯU Ý: "Nhóm sao hội hợp" trong metadata CHỈ là danh sách gợi ý phát hiện.
        AI phải TỰ XÁC ĐỊNH đây có phải "Cách cục" thật sự hay không bằng cách:
        - Kiểm tra độ sáng của sao (Miếu/Vượng hay Hãm)
        - Kiểm tra có bị Tuần/Triệt che mờ hoặc phá vỡ không
        - Kiểm tra có Tứ Hóa (Lộc/Quyền/Khoa) hỗ trợ hay (Kỵ) phá hoại không

        -------------------------------------
        BƯỚC 3b – XẾP HẠNG CÁCH CỤC (Khi phát hiện nhiều cách)

        Khi lá số đồng thời có từ 2 cách cục trở lên, AI phải so sánh để xác định cái nào là "Chủ đạo":
        ① So sánh LỰC: Cách nào tụ hội nhiều sao Miếu/Vượng hơn → mạnh hơn.
        ② So sánh VỊ TRÍ: Cách nào nằm trong tam hợp Mệnh–Tài–Quan → có tác động trực tiếp và mạnh nhất.
        ③ So sánh TỨ HÓA: Cách nào được (Hóa Lộc)/(Hóa Quyền) bản mệnh hoặc đại vận chiếu → được kích hoạt/nâng tầm.
        ④ Kết luận: Xác định đâu là "Cách cục chính" (ảnh hưởng >60% cuộc đời) và đâu là "Cách cục bổ trợ/phối hợp".

        KHÔNG ĐƯỢC luận các cách cục có sức mạnh ngang nhau nếu chúng mâu thuẫn (VD: vừa luận theo Sát Phá Tham vừa luận theo Tử Phủ Vũ Tướng mà không phân chủ-thứ).

        -------------------------------------
        BƯỚC 4 – KIỂM TRA MÂU THUẪN (BẮT BUỘC SAU KHI LUẬN 12 CUNG)

        Sau khi luận xong 12 cung, phải rà soát:
        - Mệnh vs Thân: Bẩm sinh vs Hành động có khớp không?
        - Mệnh vs Quan vs Tài: Tâm (Mệnh) - Tầm (Quan) - Lộc (Tài) logic với nhau không?
        - Phu Thê vs Phúc Đức: Duyên nợ có khớp với phúc phần không?
        - Tật Ách vs Mệnh: Sức khỏe có tương ứng với cường độ Mệnh không?

        Nếu có mâu thuẫn, phải giải thích cơ chế ưu tiên theo "Quy tắc ưu tiên khi tín hiệu mâu thuẫn" ở trên. KHÔNG ĐƯỢC để hai kết luận song song mà không phân chủ-thứ.

        =====================================
        7 PHƯƠNG PHÁP LUẬN BẮT BUỘC

        PHẦN 1: PHÂN TÍCH TỨ HÓA BẢN MỆNH
        Quy trình 8 bước: (1) Tìm vị trí 4 Hóa chủ sinh nạp -> (2) Xét Lộc/Kỵ trùng phùng -> (3) Kỵ + Sát tinh (địa kiếp, hỏa tinh...) -> (4) Lộc + Cát tinh -> (5) Hóa Kỵ rơi vào cung nào (chủ nợ/nghiệp lực) -> (6) Tứ hóa đại vận xếp chồng -> (7) Lưu Tứ Hóa -> (8) Kết luận lực Hóa.

        PHẦN 2: PHÂN TÍCH NGŨ HÀNH 4 TẦNG
        Xét sinh khắc giữa: (Tầng 1) Bản mệnh ngũ hành (Nạp âm) vs (Tầng 2) Cục vs (Tầng 3) Ngũ hành cung vs (Tầng 4) Ngũ hành sao.
        VD: Mệnh Kim đóng cung Thủy (sinh xuất) hội sao Hỏa (khắc) -> dù miếu địa cũng bị chiết giảm lực.

        PHẦN 3: QUY TẮC LUẬN THEO GIỚI TÍNH
        - Nam mệnh: Chú trọng Quan, Tài, Di. Sợ nhất: Triệt đóng Mệnh, Cô Quả hội chiếu.
        - Nữ mệnh: Chú trọng Phu, Tử, Phúc. Sợ nhất: Sát Phá Tham hội Đào Hoa/Hồng Loan/Sát tinh (dễ trắc trở tình duyên).

        PHẦN 4: QUY TRÌNH PHÂN TÍCH TUẦN – TRIỆT
        - Tuần Không: Giảm 30-50% lực sao (cát giảm cát, hung giảm hung). Ổn định dần sau 30 tuổi.
        - Triệt Không: Giảm 60-80% lực sao (ngăn chặn hoàn toàn lực mạnh nhất). Ảnh hưởng nặng nhất trước 30 tuổi.
        - Triệt tại Mệnh -> Thiếu thời lận đận.

        PHẦN 5: PHI TINH TỨ HÓA (CHUYÊN SÂU)
        Sử dụng kết quả pre-compute trong METADATA: Can cung A bay Hóa sang cung B thể hiện quan hệ nhân quả.
        - Hóa Lộc bay từ A sang B: A mang lại lợi ích/tình cảm cho B.
        - Hóa Kỵ bay từ A sang B: A gây áp lực/rắc rối/phiền muộn cho B.
        - Tự hóa (A hóa cho chính A): Cung đó có xu hướng tự tan biến hoặc tự mâu thuẫn.

        PHẦN 6: VẬN HẠN ĐA TẦNG (XẾP CHỒNG)
        Quy trình 5 bước: (1) Xác định Mệnh Đại Vận -> (2) Tìm Tứ Hóa Đại Vận -> (3) Tìm Lưu Niên Tứ Hóa năm xem -> (4) Tìm "Trùng điệp" (VD: Song Kỵ, Song Lộc hội tụ một cung) -> (5) Xét cung Tiểu Hạn.

        PHẦN 7: KIỂM CHỨNG CHÉO (CROSS-CHECK)
        - Luôn đối chiếu Tam giác Mệnh-Quan-Tài để xem "Cái Tâm và Cái Tầm".
        - Đối chiếu Mệnh (bẩm sinh) vs Thân (hành động hậu thiên).
        - Nếu mâu thuẫn (Mệnh tốt Thân xấu) -> Tiền cát hậu hung.

        =====================================
        PHƯƠNG PHÁP LUẬN MỖI CUNG

        Bước 1
        Xác định chính tinh + trạng thái miếu / vượng / đắc / hãm.

        Bước 2
        Liệt kê phụ tinh quan trọng và tứ hóa.

        Bước 3
        Phân tích tương tác:

        đồng cung
        tam hợp
        xung chiếu
        giáp cung
        hội sát tinh

        QUY TẮC TRỌNG SỐ TƯƠNG TÁC:
        - Đồng cung: 100% lực (mạnh nhất)
        - Tam hợp hội chiếu: 70-80% lực
        - Xung chiếu (đối cung): 60-70% lực (ảnh hưởng gián tiếp)
        - Giáp cung: 40-50% lực (hỗ trợ/kìm hãm từ hai bên)
        - Nhị hợp: 30% lực (yếu nhất)
        ⚠️ Sát tinh xung chiếu gây hại ÍT HƠN sát tinh đồng cung.
        ⚠️ Cát tinh tam hợp hội chiếu có lực MẠNH HƠN cát tinh giáp cung.

        Bước 4
        Đánh giá lực cung:

        mạnh / trung / yếu
        thuận / nghịch

        Bước 5
        Chuyển sang biểu hiện thực tế:

        tính cách
        nghề nghiệp
        tài chính
        quan hệ
        sức khỏe
        tâm lý

        =====================================
        CẤU TRÚC LUẬN (BẮT BUỘC THEO THỨ TỰ)

        1. MỆNH (phân tích kỹ nhất, bao gồm Mệnh – Thân – Cục)

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

        =====================================
        FORMAT KẾT LUẬN

        Mỗi nhận định phải theo cấu trúc:

        [Cung]

        (Căn cứ: sao A, sao B, trạng thái miếu/vượng/hãm,
        tam hợp cung X có sao Y,
        bị sát tinh Z phá hoặc được cát tinh K nâng…)

        → phân tích logic tinh hệ

        → biểu hiện thực tế.
        → 🔹 LỰC CUNG: [1-10] (1=rất yếu, 10=rất mạnh)
        → 🔹 XU HƯỚNG: [Thuận/Nghịch/Biến động]

        Mỗi kết luận lớn (cách cục, vận hạn) phải kèm:
        📊 ĐỘ TIN CẬY: [Cao/Trung bình/Thấp]
        - Cao: ≥3 căn cứ tinh hệ khớp nhau, không mâu thuẫn.
        - Trung bình: 1-2 căn cứ, hoặc có mâu thuẫn nhẹ.
        - Thấp: Thiếu dữ liệu hoặc nhiều mâu thuẫn.

        Ví dụ:

        "Tử Vi + Thiên Phủ miếu địa hội Tả Hữu Xương Khúc
        → tinh hệ quản trị mạnh
        → dễ nắm quyền tổ chức."

        "Sát Phá Tham hội Kình Đà + Hóa Kỵ
        → biến động mạnh
        → quyết đoán cao nhưng rủi ro tài chính."

        =====================================
        PHÂN LOẠI LÁ SỐ

        Sau khi luận xong phải xác định lá số thuộc nhóm nào:

        • Đại phú
        • Đại quý
        • Phú quý nhờ vận
        • Giàu nhưng lao tâm
        • Quyền lực
        • Học thuật
        • Bạo phát
        • Khởi nghiệp thành công

        Chỉ được kết luận khi có căn cứ tinh hệ.

        =====================================
        FORMAT ĐẦU RA

        A. TÓM TẮT LÁ SỐ (5–10 dòng)

        • tinh hệ nổi bật
        • điểm mạnh
        • điểm yếu
        • căn cứ sao

        B. LUẬN CHI TIẾT 12 CUNG

        C. CÁCH CỤC LỚN

        D. PHÂN LOẠI LÁ SỐ

        E. KẾT LUẬN TỔNG THỂ

        • sức mạnh tổng thể lá số
        • khả năng giàu có
        • khả năng quyền lực
        • hướng phát triển sự nghiệp

        E1. Vận năm ${info.viewingYear} (BẮT BUỘC – KHÔNG ĐƯỢC BỎ QUA)
        Phân tích theo thứ tự:
        (1) Đại vận hiện tại → ảnh hưởng nền
        (2) Lưu niên ${info.viewingYear} → sao lưu + lưu tứ hóa
        (3) Trùng điệp tứ hóa → Song Lộc/Song Kỵ/Lộc Kỵ giao nhau
        (4) Tác động lên Mệnh – Quan – Tài – Phu Thê
        (5) Kết luận: thuận lợi / rủi ro chính trong năm

        =====================================

        QUY ƯỚC KÝ HIỆU TRONG DỮ LIỆU:
        • (M) = Miếu, (V) = Vượng, (Đ) = Đắc, (Bình) = Bình, (H) = Hãm — trạng thái của chính tinh
        • (Hóa Lộc), (Hóa Quyền), (Hóa Khoa), (Hóa Kỵ) — Tứ hóa bản mệnh
        • ĐV. = Sao Đại Vận (VD: ĐV. Lộc Tồn, ĐV. Hóa Lộc = Đại Vận Hóa Lộc)
        • L. = Sao Lưu niên (VD: L.Kình Dương, L.Hóa Kỵ = Lưu niên Hóa Kỵ)
<<<<<<< HEAD
=======
        • LN. = Sao Lưu nguyệt (VD: LN. Khôi Việt, LN. Hóa Lộc = Lưu nguyệt Hóa Lộc)
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))
        • Tuần, Triệt = Tuần Không và Triệt Không (sao bị Tuần/Triệt sẽ giảm lực)
        • QUY TẮC VÔ CHÍNH DIỆU (4 bước):
          Bước 1: Mượn chính tinh cung đối chiếu (xung chiếu) — giảm 30% lực so với sao ở bản cung.
          Bước 2: Phụ tinh trong cung vô chính diệu trở thành "chủ thực tế" — phân tích kỹ hơn.
          Bước 3: Vô chính diệu + nhiều sát tinh → cung rất yếu, biến động lớn.
          Bước 4: Vô chính diệu + nhiều cát tinh → "đất trống gặp mưa" — muộn phát nhưng có thể phát.
        • "Tam hợp [Bộ sao]" = Các sao phân bố đều trên 3 cung thuộc mạng lưới tam hợp.
        • "Nhóm [Bộ sao]" = Các sao có xuất hiện hội tụ nhưng CHƯA đủ điều kiện hoặc phân bố chưa chuẩn để gọi là cách cục hoàn chỉnh (cần AI đánh giá thêm).

        CÁC LỖI PHỔ BIẾN AI KHÔNG ĐƯỢC MẮC:
        ❌ SAI: "Tử Vi là sao vua nên ở đâu cũng tốt" → PHẢI xét miếu/hãm, cung vị.
        ❌ SAI: "Kình Dương luôn xấu" → Kình Dương miếu (VD: Ngọ) có thể tạo Mã Đầu Đới Kiếm.
        ❌ SAI: "Hóa Kỵ luôn xấu" → Kỵ ở Quan/Tài có thể chỉ là "chuyên tâm, bám víu".
        ❌ SAI: Luận Vô Chính Diệu mà không nhắc chính tinh đối cung.
        ❌ SAI: Gộp cát tinh + sát tinh → "trung bình" → PHẢI phân tích cơ chế: cát giảm sát hay sát phá cát.

        Nội dung lá số:

        $lasoContent
        """.trimIndent()
    }

    private fun buildTuHoaSummary(cungList: List<CungInfo>): String {
        val sb = StringBuilder()
        
        // 1. Tứ Hóa Bản Mệnh
        sb.appendLine("TỨ HÓA BẢN MỆNH:")
        val bmSuffixes = listOf("(Hóa Lộc)", "(Hóa Quyền)", "(Hóa Khoa)", "(Hóa Kỵ)")
        for (suffix in bmSuffixes) {
            val cung = cungList.find { c -> c.phuTinh.contains(suffix.trim()) }
            if (cung != null) {
                sb.appendLine("  $suffix → Cung ${cung.name} (${cung.chucNang})")
            }
        }
        
        // 2. Tứ Hóa Đại Vận
        sb.appendLine("TỨ HÓA ĐẠI VẬN:")
        val dvSuffixes = listOf("(ĐV. Hóa Lộc)", "(ĐV. Hóa Quyền)", "(ĐV. Hóa Khoa)", "(ĐV. Hóa Kỵ)")
        for (suffix in dvSuffixes) {
            val cung = cungList.find { c -> c.phuTinh.contains(suffix.trim()) }
            if (cung != null) {
                sb.appendLine("  $suffix → Cung ${cung.name} (${cung.chucNang})")
            }
        }
        
        // 3. Tứ Hóa Lưu Niên
        sb.appendLine("TỨ HÓA LƯU NIÊN:")
        val lnSuffixes = listOf("(L.Hóa Lộc)", "(L.Hóa Quyền)", "(L.Hóa Khoa)", "(L.Hóa Kỵ)")
        for (suffix in lnSuffixes) {
            val cung = cungList.find { c -> c.phuTinh.contains(suffix.trim()) }
            if (cung != null) {
                sb.appendLine("  $suffix → Cung ${cung.name} (${cung.chucNang})")
            }
        }
        
        return sb.toString()
    }

    private fun detectBoSao(cungList: List<CungInfo>): List<String> {
        val result = mutableListOf<String>()

        fun findStar(name: String): Int? {
            return cungList.indexOfFirst { cung ->
                cung.chinhTinh.any { it.startsWith(name) }
            }.takeIf { it >= 0 }
        }

        // Helper: Get indices of tam hợp for a palace
        fun getTamHopIndices(idx: Int): Set<Int> {
            return setOf(idx, (idx + 4) % 12, (idx + 8) % 12)
        }

        // Helper: Get indices of tam phương tứ chính (hội hợp) for a palace
        fun getHoiHopIndices(idx: Int): Set<Int> {
            return setOf(idx, (idx + 4) % 12, (idx + 8) % 12, (idx + 6) % 12)
        }

        // 1. Sát Phá Tham
        val satIdx = findStar("Thất Sát")
        val phaIdx = findStar("Phá Quân")
        val thamIdx = findStar("Tham Lang")
        if (satIdx != null && phaIdx != null && thamIdx != null) {
            val group = getTamHopIndices(satIdx)
            if (phaIdx in group && thamIdx in group) {
                result.add("Tam hợp Sát Phá Tham (${Constants.DIA_CHI[satIdx]}–${Constants.DIA_CHI[phaIdx]}–${Constants.DIA_CHI[thamIdx]})")
            }
        }

        // 2. Tử Phủ Vũ Tướng
        val tuViIdx = findStar("Tử Vi")
        val phuIdx = findStar("Thiên Phủ")
        val vuIdx = findStar("Vũ Khúc")
        val tuongIdx = findStar("Thiên Tướng")
        if (tuViIdx != null && phuIdx != null && vuIdx != null && tuongIdx != null) {
            val idxs = listOf(tuViIdx, phuIdx, vuIdx, tuongIdx)
            val uniqueIdxs = idxs.distinct().sorted()
            val group = getHoiHopIndices(tuViIdx)
            if (idxs.all { it in group }) {
                // Traditionally, if stars cluster in only 2 palaces, it's not a strong formation
                val label = if (uniqueIdxs.size < 3) "Nhóm Tử Phủ Vũ Tướng" else "Tử Phủ Vũ Tướng hội chiếu"
                val cungStrs = uniqueIdxs.joinToString("–") { Constants.DIA_CHI[it] }
                result.add("$label ($cungStrs)")
            }
        }

        // 3. Cơ Nguyệt Đồng Lương
        val coIdx = findStar("Thiên Cơ")
        val nguyetIdx = findStar("Thái Âm")
        val dongIdx = findStar("Thiên Đồng")
        val luongIdx = findStar("Thiên Lương")
        if (coIdx != null && nguyetIdx != null && dongIdx != null && luongIdx != null) {
            val idxs = listOf(coIdx, nguyetIdx, dongIdx, luongIdx)
            val uniqueIdxs = idxs.distinct().sorted()
            val group = getHoiHopIndices(coIdx)
            if (idxs.all { it in group }) {
                val label = if (uniqueIdxs.size < 3) "Nhóm Cơ Nguyệt Đồng Lương" else "Cơ Nguyệt Đồng Lương hội chiếu"
                val cungStrs = uniqueIdxs.joinToString("–") { Constants.DIA_CHI[it] }
                result.add("$label ($cungStrs)")
            }
        }

        // 4. Nhật Nguyệt (Thái Dương + Thái Âm)
        val nhatIdx = findStar("Thái Dương")
        if (nhatIdx != null && nguyetIdx != null) {
            val label = when {
                nhatIdx == nguyetIdx -> "Nhật Nguyệt đồng cung"
                Math.abs(nhatIdx - nguyetIdx) == 6 -> "Nhật Nguyệt đối chiếu"
                Math.abs(nhatIdx - nguyetIdx) % 4 == 0 -> "Nhật Nguyệt hội chiếu"
                else -> null
            }
            if (label != null) {
                val cungStrs = if (nhatIdx == nguyetIdx) "tại ${Constants.DIA_CHI[nhatIdx]}" else "${Constants.DIA_CHI[nhatIdx]}–${Constants.DIA_CHI[nguyetIdx]}"
                result.add("$label ($cungStrs)")
            }
        }

        return result
    }

    // =============================================
    // JSON PROMPT BUILDER HELPERS
    // =============================================

    private fun buildAbsoluteRulesJson(data: LasoData): JSONObject {
        val info = data.info
        val birthYear = info.solarDate.split("/").last().toIntOrNull() ?: info.viewingYear
        val approxAge = info.viewingYear - birthYear + 1
        val isChild = approxAge < 13 || info.daiVanInfo.contains("Chưa vào đại vận")

        return JSONObject().apply {
            put("must_do", JSONArray(listOf(
                "Mọi nhận định BẮT BUỘC phải có căn cứ sao",
                "Phân biệt rõ: Chính tinh, Phụ tinh, Cát tinh, Sát tinh, Tứ hóa, Sao lưu, Sao đại vận",
                "Không bỏ qua tương tác tinh hệ: đồng cung, tam hợp, xung chiếu, hội chiếu, giáp cung",
                "Sát tinh phải phân tích theo cơ chế: sát+cát, sát+vận, sát phá cách hay tạo đột phá",
                "Phải dùng ngôn ngữ xác suất: 'thường', 'có xu hướng', 'nếu vận hỗ trợ'"
            )))
            put("must_not", JSONArray(listOf(
                "Không dùng câu chung chung ('số giàu', 'số khổ') nếu không chỉ rõ tinh hệ và cơ chế",
                "Không suy đoán khi thiếu dữ liệu. Thiếu thông tin → nêu rõ 'Không có trong dữ liệu được cung cấp' và bỏ qua phần không đủ căn cứ",
                "Không thần bí hóa sát tinh",
                "Không khẳng định tuyệt đối"
            )))
            put("data_integrity", JSONObject().apply {
                put("forbidden", JSONArray(listOf(
                    "Tự tính miếu/vượng/đắc/bình/hãm (phải dùng ký hiệu M/V/Đ/Bình/H có sẵn)",
                    "Tự xác định đại vận khi input chưa cung cấp",
                    "Tự tính lưu tinh, lưu tứ hóa hoặc sao vận khi input chưa cung cấp",
                    "Tự thêm sao, tứ hóa, trạng thái sáng tối",
                    "Tự kết luận cách cục nếu không đủ sao và điều kiện thực tế"
                )))
                put("allowed", JSONArray(listOf(
                    "Đánh giá lực cung 1-10 dựa trên tổ hợp sao + trạng thái + tứ hóa + Tuần/Triệt đã có sẵn",
                    "Xếp hạng chủ-thứ giữa nhiều cách cục khi sao và điều kiện đã có trong dữ liệu",
                    "Suy luận mạnh/yếu, thuận/nghịch, phá cách hay hỗ trợ dựa trên quy tắc ưu tiên"
                )))
                put("fallback", "Nếu thiếu dữ liệu → ghi rõ: 'Không có trong dữ liệu được cung cấp'")
            })
            if (isChild) {
                put("child_rule", JSONObject().apply {
                    put("condition", "Đương số dưới 13 tuổi hoặc chưa vào đại vận")
                    put("forbidden_topics", JSONArray(listOf("Tiền Bạc (Tài bạch)", "Sự Nghiệp (Quan lộc)", "Tình Duyên (Phu thê)")))
                    put("focus_topics", JSONArray(listOf("Sức khỏe", "Tính cách bẩm sinh", "Khả năng tiếp thu/học tập", "Môi trường cha mẹ nuôi dưỡng (cung Phụ Mẫu)")) )
                    put("tone", "Tư vấn cho Phụ huynh. Sử dụng ngôn ngữ định hướng. VD: 'Bé có xu hướng...', 'Cha mẹ nên lưu ý...'")
                    put("_note", "AI PHẢI bỏ qua mọi logic về công danh, tiền bạc nếu child_rule được kích hoạt.")
                })
            }
        }
    }

    private fun buildAxisMappingJson(cungList: List<CungInfo>): JSONObject {
        val mapping = JSONObject()
        // 6 trục đối xung chuẩn trong Tử Vi (mỗi trục = 2 cung đối nhau qua tâm lá số)
        val axes = listOf(
            "Mệnh-Di" to Pair("Mệnh", "Thiên Di"),
            "Phụ-Ách" to Pair("Phụ Mẫu", "Tật Ách"),
            "Phúc-Tài" to Pair("Phúc Đức", "Tài Bạch"),
            "Điền-Tử" to Pair("Điền Trạch", "Tử Tức"),
            "Quan-Thê" to Pair("Quan Lộc", "Phu Thê"),
            "Nô-Huynh" to Pair("Nô Bộc", "Huynh Đệ")
        )
        axes.forEach { (name, pair) ->
            val p1 = cungList.find { it.chucNang.contains(pair.first) }
            val p2 = cungList.find { it.chucNang.contains(pair.second) }
            if (p1 != null && p2 != null) {
                mapping.put(name, JSONObject().apply {
                    put("p1", "${p1.name} (${p1.chucNang})")
                    put("p2", "${p2.name} (${p2.chucNang})")
                })
            }
        }
        return mapping
    }

    private fun buildPipelineJson(): JSONObject {
        return JSONObject().apply {
            put("step_1_summary", JSONObject().apply {
                put("name", "Tóm tắt cấu trúc lá số")
                put("items", JSONArray(listOf("Chính tinh từng cung", "Tứ hóa", "Cung Mệnh", "Cung Thân", "Tam hợp Mệnh–Tài–Quan")))
            })
            put("step_2_power", JSONObject().apply {
                put("name", "Đánh giá lực lá số")
                put("items", JSONArray(listOf("Mệnh mạnh hay yếu", "Thân cư cung nào", "Cục sinh hay khắc mệnh", "Sát tinh nặng hay không", "Cát tinh nâng đỡ không")))
            })
            put("step_3_configurations", JSONObject().apply {
                put("name", "Kiểm tra cách cục")
                put("major_list", JSONArray(listOf("Tử Phủ Vũ Tướng", "Phủ Tướng Triều Viên", "Cơ Nguyệt Đồng Lương", "Nhật Nguyệt Tịnh Minh", "Sát Phá Tham", "Liêm Tham", "Cự Nhật", "Vũ Khúc tài tinh", "Thiên Phủ tài khố", "Thái Âm tài tinh")))
                put("extended", JSONObject().apply {
                    put("dai_quy", JSONArray(Constants.CACH_CUC_DAI_QUY))
                    put("dai_phu", JSONArray(Constants.CACH_CUC_DAI_PHU))
                    put("vo", JSONArray(Constants.CACH_CUC_VO))
                    put("hung_pha", JSONArray(Constants.CACH_CUC_HUNG))
                    put("dac_biet", JSONArray(Constants.CACH_CUC_DAC_BIET))
                })
                put("validation_warning", "Nhóm sao hội hợp trong metadata CHỈ là gợi ý. AI TỰ XÁC ĐỊNH tính hợp lệ của cách cục BẰNG CÁCH: (1) Dùng trạng thái M/V/Đ/Bình/H CÓ SẴN trong dữ liệu sao, (2) Kiểm tra Tuần/Triệt đã được ghi nhận tại cung (flags hoặc sao 'Tuần'/'Triệt' trong palace data), (3) Xem tứ hóa đã cung cấp. KHÔNG tự tính thêm trạng thái mới. Nếu thiếu dữ liệu trạng thái → nhận định 'không đủ căn cứ'.")
                put("tuan_triet_check_rule", "BẮT BUỘC quét toàn bộ 12 cung khi đánh giá cách cục và vận hạn: dùng flags 'Gặp Tuần'/'Gặp Triệt' và sao 'Tuần'/'Triệt' có sẵn trong palace data. KHÔNG tự tính vị trí Tuần/Triệt ngoài dữ liệu đã cung cấp. LƯU Ý: flags và sao Tuần/Triệt trong danh sách sao là CÙNG MỘT hiện tượng, CHỈ TÍNH 1 LẦN.")
                put("check_sat_tinh_pha_cach", true)
            })
            put("step_3b_ranking", JSONObject().apply {
                put("name", "Xếp hạng cách cục (khi ≥2 cách)")
                put("criteria", JSONArray(listOf(
                    "① So sánh LỰC: Cách nào nhiều sao Miếu/Vượng → mạnh hơn",
                    "② So sánh VỊ TRÍ: Cách nào nằm tam hợp Mệnh–Tài–Quan → trực tiếp nhất",
                    "③ So sánh TỨ HÓA: Cách nào được Hóa Lộc/Quyền bản mệnh hoặc đại vận chiếu → nâng tầm",
                    "④ Kết luận: 'Cách cục chính' (xu hướng chi phối chính) vs 'Cách bổ trợ'"
                )))
                put("forbidden", "KHÔNG luận ngang nhau nếu mâu thuẫn mà không phân chủ-thứ")
            })
            put("step_4_contradiction_check", JSONObject().apply {
                put("name", "Kiểm tra mâu thuẫn (BẮT BUỘC sau 12 cung)")
                put("pairs", JSONArray(listOf(
                    "Mệnh vs Thân: Bẩm sinh vs Hành động khớp không?",
                    "Mệnh vs Quan vs Tài: Tâm – Tầm – Lộc logic không?",
                    "Phu Thê vs Phúc Đức: Duyên nợ khớp phúc phần không?",
                    "Tật Ách vs Mệnh: Sức khỏe tương ứng cường độ Mệnh không?"
                )))
                put("resolution", "Giải thích theo quy tắc ưu tiên. KHÔNG để hai kết luận song song không phân chủ-thứ")
            })
        }
    }

    private fun buildMethodsJson(): JSONObject {
        return JSONObject().apply {
            put("m1_tu_hoa", JSONObject().apply {
                put("name", "Phân tích Tứ Hóa Bản Mệnh")
                put("steps", JSONArray(listOf(
                    "(1) Tìm vị trí 4 Hóa chủ sinh nạp", "(2) Xét Lộc/Kỵ trùng phùng",
                    "(3) Kỵ + Sát tinh (địa kiếp, hỏa tinh...)", "(4) Lộc + Cát tinh",
                    "(5) Hóa Kỵ rơi cung nào (chủ nợ/nghiệp lực)", "(6) Tứ hóa đại vận xếp chồng",
                    "(7) Lưu Tứ Hóa", "(8) Kết luận lực Hóa"
                )))
            })
            put("m2_ngu_hanh", JSONObject().apply {
                put("name", "Phân tích Ngũ Hành 4 tầng")
                put("layers", JSONArray(listOf("Nạp âm bản mệnh", "Cục", "Ngũ hành cung", "Ngũ hành sao")))
                put("example", "Mệnh Kim đóng cung Thủy (sinh xuất) hội sao Hỏa (khắc) → dù miếu cũng chiết giảm lực")
            })
            put("m3_gender", JSONObject().apply {
                put("name", "Gợi ý luận theo giới tính (truyền thống, tùy chọn)")
                put("_note", "Đây là quy tắc truyền thống, CHỈ áp dụng khi phù hợp ngữ cảnh. Không bắt buộc.")
                put("male", JSONObject().apply {
                    put("focus", JSONArray(listOf("Quan", "Tài", "Di")))
                    put("fear", "Triệt đóng Mệnh, Cô Quả hội chiếu")
                })
                put("female", JSONObject().apply {
                    put("focus", JSONArray(listOf("Phu", "Tử", "Phúc")))
                    put("fear", "Sát Phá Tham hội Đào Hoa/Hồng Loan/Sát tinh (dễ trắc trở tình duyên)")
                })
            })
            put("m4_tuan_triet", JSONObject().apply {
                put("name", "Phân tích Tuần – Triệt")
                put("tuan", "Thường làm giảm đáng kể lực sao (cát giảm cát, hung giảm hung). Xu hướng ổn định dần sau 30 tuổi")
                put("triet", "Thường làm giảm mạnh lực sao (có thể triệt tiêu phần lớn lực). Xu hướng ảnh hưởng rõ nhất trước 30 tuổi")
                put("triet_at_menh", "Thường gặp khó khăn giai đoạn đầu đời")
            })
            put("m5_phi_tinh", JSONObject().apply {
                put("name", "Phi Tinh Tứ Hóa (chuyên sâu)")
                put("source", "chart_data.phi_tinh_tu_hoa (pre-computed: cung_nguồn → cung_nhận Lộc/Quyền/Khoa/Kỵ)")
                put("rules", JSONObject().apply {
                    put("loc_a_to_b", "A mang lại lợi ích/tình cảm cho B")
                    put("ky_a_to_b", "A gây áp lực/rắc rối/phiền muộn cho B")
                    put("tu_hoa", "Cung tự tan biến hoặc tự mâu thuẫn")
                })
            })
            put("m6_fortune_layers", JSONObject().apply {
                put("name", "Vận hạn đa tầng (xếp chồng)")
                put("steps", JSONArray(listOf(
                    "(1) Xác định Mệnh Đại Vận", "(2) Tìm Tứ Hóa Đại Vận",
                    "(3) Tìm Lưu Niên Tứ Hóa năm xem",
                    "(4) Tìm Trùng điệp (Song Kỵ, Song Lộc hội tụ một cung)",
                    "(5) Xét cung Tiểu Hạn"
                )))
            })
            put("m7_cross_check", JSONObject().apply {
                put("name", "Kiểm chứng chéo")
                put("checks", JSONArray(listOf(
                    "Tam giác Mệnh-Quan-Tài: Cái Tâm và Cái Tầm",
                    "Mệnh (bẩm sinh) vs Thân (hành động hậu thiên)",
                    "Mệnh tốt Thân xấu → Có thể thuận lợi giai đoạn đầu, biến động về sau"
                )))
            })
        }
    }

    private fun buildPalaceMethodJson(): JSONObject {
        return JSONObject().apply {
            put("steps", JSONArray(listOf(
                "Xác định chính tinh + trạng thái miếu/vượng/đắc/hãm",
                "Liệt kê phụ tinh quan trọng và tứ hóa",
                "Phân tích tương tác: đồng cung, tam hợp, xung chiếu, giáp cung, hội sát tinh",
                "Đánh giá lực cung: mạnh/trung/yếu, thuận/nghịch",
                "Chuyển sang biểu hiện thực tế: tính cách, nghề nghiệp, tài chính, quan hệ, sức khỏe, tâm lý"
            )))
            put("interaction_weights", JSONObject().apply {
                put("dong_cung", JSONObject().apply { put("value", 1.0); put("note", "100% lực – mạnh nhất") })
                put("tam_hop", JSONObject().apply { put("value", 0.75); put("note", "70-80% lực") })
                put("xung_chieu", JSONObject().apply { put("value", 0.65); put("note", "60-70% lực – ảnh hưởng gián tiếp") })
                put("giap_cung", JSONObject().apply { put("value", 0.45); put("note", "40-50% lực – hỗ trợ/kìm hãm từ hai bên") })
                put("nhi_hop", JSONObject().apply { put("value", 0.3); put("note", "Yếu nhất") })
            })
            put("warnings", JSONArray(listOf(
                "Sát tinh xung chiếu gây hại ÍT HƠN sát tinh đồng cung",
                "Cát tinh tam hợp hội chiếu có lực MẠNH HƠN cát tinh giáp cung"
            )))
        }
    }

    private fun buildOutputFormatJson(data: LasoData): JSONObject {
        val info = data.info
        val vanHanRequest = if (info.viewingMode == "MONTH") {
            "Vận tháng ${info.viewingMonth} năm ${info.viewingYear}"
        } else {
            "Vận năm ${info.viewingYear}"
        }
        return JSONObject().apply {
            put("sections", JSONObject().apply {
                put("A", "Tóm tắt lá số (5-10 dòng): tinh hệ nổi bật, điểm mạnh, điểm yếu, căn cứ sao")
                put("B", "Luận chi tiết 12 cung")
                put("C", "Cách cục lớn")
                put("D", "Phân loại lá số")
                put("E", "Kết luận tổng thể: sức mạnh, khả năng giàu có, quyền lực, hướng sự nghiệp")
                put("E1", "$vanHanRequest (BẮT BUỘC – KHÔNG ĐƯỢC BỎ QUA)")
            })
            put("palace_format", JSONObject().apply {
                put("structure", "[Cung] → (Căn cứ: sao, trạng thái, tam hợp, sát/cát) → Logic tinh hệ → Biểu hiện thực tế")
                put("force_score", "1-10 (1=rất yếu, 10=rất mạnh)")
                put("trend", JSONArray(listOf("Thuận", "Nghịch", "Biến động")))
                put("confidence", JSONObject().apply {
                    put("Cao", "≥3 căn cứ tinh hệ khớp, không mâu thuẫn")
                    put("Trung bình", "1-2 căn cứ, hoặc mâu thuẫn nhẹ")
                    put("Thấp", "Thiếu dữ liệu hoặc nhiều mâu thuẫn")
                })
            })
            put("classification_categories", JSONArray(listOf(
                "Đại phú", "Đại quý", "Phú quý nhờ vận", "Giàu nhưng lao tâm",
                "Quyền lực", "Học thuật", "Bạo phát", "Khởi nghiệp thành công"
            )))
            put("fortune_period_format", JSONObject().apply {
                put("steps", JSONArray(listOf(
                    "(1) Đại vận hiện tại → ảnh hưởng nền",
                    "(2) Lưu niên ${info.viewingYear} → sao lưu + lưu tứ hóa",
                    "(3) Trùng điệp tứ hóa → Song Lộc/Song Kỵ/Lộc Kỵ giao nhau",
                    "(4) Tác động lên Mệnh – Quan – Tài – Phu Thê",
<<<<<<< HEAD
                    "(5) Kết luận theo dữ liệu sẵn có. Nếu thiếu dữ liệu lưu nguyệt → chỉ kết luận ở mức năm"
=======
                    "(5) Nếu xem tháng (MONTH), sử dụng dữ liệu Cung Lưu Nguyệt, LN. Tứ Hóa và Sao Lưu Nguyệt (LN.) để luận chi tiết"
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))
                )))
            })
        }
    }

    private fun buildNotationJson(): JSONObject {
        return JSONObject().apply {
            put("brightness", JSONObject().apply {
                put("M", "Miếu"); put("V", "Vượng"); put("Đ", "Đắc"); put("Bình", "Bình"); put("H", "Hãm")
                put("_note", "Sao không có field 'state' thường là tiểu tinh không xét miếu/hãm — coi như trung tính (không cộng/trừ lực).")
            })
            put("transformations", JSONObject().apply {
                put("ban_menh", "(Hóa Lộc), (Hóa Quyền), (Hóa Khoa), (Hóa Kỵ)")
                put("dai_van", "ĐV. prefix")
                put("luu_nien", "L. prefix")
<<<<<<< HEAD
=======
                put("luu_nguyet", "LN. prefix")
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))
            })
            put("specials", JSONArray(listOf("Tuần = Tuần Không (giảm lực)", "Triệt = Triệt Không (giảm lực)")))
            put("vo_chinh_dieu_rules", JSONArray(listOf(
                "Bước 1: Mượn chính tinh cung đối chiếu (xung chiếu) — giảm 30% lực so với sao ở bản cung",
                "Bước 2: Phụ tinh trong cung vô chính diệu trở thành 'chủ thực tế' — phân tích kỹ hơn",
                "Bước 3: Vô chính diệu + nhiều sát tinh → cung rất yếu, biến động lớn",
                "Bước 4: Vô chính diệu + nhiều cát tinh → 'đất trống gặp mưa' — muộn phát nhưng có thể phát"
            )))
            put("group_labels", JSONObject().apply {
                put("tam_hop", "Sao phân bố đều trên 3 cung tam hợp")
                put("nhom", "Sao hội tụ nhưng CHƯA đủ điều kiện cách cục (cần AI đánh giá thêm)")
            })
        }
    }

    private fun buildMistakesJson(): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply { put("wrong", "Tử Vi là sao vua nên ở đâu cũng tốt"); put("correct", "PHẢI xét miếu/hãm, cung vị") })
            put(JSONObject().apply { put("wrong", "Kình Dương luôn xấu"); put("correct", "Kình Dương miếu (Ngọ) có thể tạo Mã Đầu Đới Kiếm") })
            put(JSONObject().apply { put("wrong", "Hóa Kỵ luôn xấu"); put("correct", "Kỵ ở Quan/Tài có thể chỉ là 'chuyên tâm, bám víu'") })
            put(JSONObject().apply { put("wrong", "Luận Vô Chính Diệu mà không nhắc chính tinh đối cung"); put("correct", "PHẢI nhắc chính tinh đối cung") })
            put(JSONObject().apply { put("wrong", "Gộp cát tinh + sát tinh → 'trung bình'"); put("correct", "PHẢI phân tích cơ chế: cát giảm sát hay sát phá cát") })
        }
    }

    private fun buildChartDataJson(data: LasoData): JSONObject {
        val info = data.info
        val cungList = data.cung

        // Reuse existing helpers
        val boSaoList = detectBoSao(cungList)
        val nguHanhSaoObj = JSONObject()
        Constants.NGU_HANH_SAO.forEach { (sao, hanh) -> nguHanhSaoObj.put(sao, hanh) }

        // Axis Mapping in ChartData (NEW v3.0)
        val axisMapping = buildAxisMappingJson(cungList)

        // Can Chi 12 cung
        val canChi12Obj = JSONObject()
        cungList.forEach { canChi12Obj.put(it.name, it.canChi) }

        // Van Han request
        val vanHanRequest = if (info.viewingMode == "MONTH") {
            "Phân tích vận tháng ${info.viewingMonth} âm lịch năm ${info.viewingYear}. " +
<<<<<<< HEAD
            "Sử dụng: đại vận + tiểu hạn + lưu niên tứ hóa đã cung cấp. " +
            "LƯU Ý: Dữ liệu hiện tại là cấp NĂM, chưa có lưu nguyệt tứ hóa. " +
            "Nếu không đủ căn cứ cho kết luận cấp tháng → nêu rõ giới hạn và luận ở mức năm."
=======
            "Sử dụng: đại vận + tiểu hạn + lưu niên tứ hóa + Cung Lưu Nguyệt + LN. Tứ Hóa và Sao Lưu Nguyệt (tiền tố LN.) để kết luận."
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))
        } else {
            "Phân tích vận năm ${info.viewingYear} (theo đại vận hiện tại và lưu tinh năm)"
        }

        return JSONObject().apply {
            put("_role", "Đây là dữ liệu lá số THỰC TẾ của đương số. PHẢI dùng để phân tích, KHÔNG phải ví dụ.")
            // Person
            put("person", JSONObject().apply {
                put("name", info.name)
                put("gender", info.gender)
                put("birth_solar", "${info.solarDate} lúc ${info.time}")
                put("birth_lunar", "${info.lunarDate} (${info.canChi})")
                put("cuc", info.cuc)
                put("menh_position", info.menhTai)
                put("than_position", info.thanTai)
                put("dai_van", info.daiVanInfo)
                put("viewing_period", if (info.viewingMode == "MONTH") "Tháng ${info.viewingMonth} năm ${info.viewingYear}" else "Năm ${info.viewingYear}")
            })

            // Metadata
            put("metadata", JSONObject().apply {
                put("menh_ngu_hanh", info.menhNguHanh)
                put("am_duong", info.amDuong)
                put("cuc_menh_relation", info.cucMenhRelation)
                put("ngu_hanh_14_sao", nguHanhSaoObj)
                put("nhom_sao", if (boSaoList.isEmpty()) JSONArray(listOf("Không phát hiện")) else JSONArray(boSaoList))
                put("axis_mapping", axisMapping)
                put("can_chi_12_cung", canChi12Obj)
                put("tieu_han_cung", "${info.tieuHanCung} (năm ${info.viewingYear})")
<<<<<<< HEAD
=======
                if (info.luuNguyetCung.isNotEmpty()) {
                    put("luu_nguyet_cung", "${info.luuNguyetCung} (tháng ${info.viewingMonth})")
                }
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))
                put("dai_van_list", parseDaiVanToJson(info.daiVanFullList))
            })

            // Phi Tinh
            put("phi_tinh_tu_hoa", if (info.phiTinhTuHoa.isNotEmpty()) parsePhiTinhToJson(info.phiTinhTuHoa) else JSONObject().apply { put("status", "Không có dữ liệu phi tinh") })
<<<<<<< HEAD
=======
            if (info.phiTinhLuuNguyet.isNotEmpty()) {
                put("phi_tinh_luu_nguyet", info.phiTinhLuuNguyet)
            }
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))

            // Tu Hoa Summary (Marked as note for AI to cross-check palace data)
            put("tu_hoa_summary", buildTuHoaSummaryJson(cungList).apply {
                put("_note", "Đây là bản tóm tắt nhanh. Dữ liệu gốc nằm trong fixed_stars và transit_stars của từng cung trong 'palaces'.")
            })

            // Tu Hoa 10 Can Table
            put("tu_hoa_10_can", JSONObject().apply {
                put("_warning", "CHỈ dùng GIẢI THÍCH cơ chế phi tinh. KHÔNG dùng để tự tính thêm tứ hóa")
                Constants.THIEN_CAN.forEachIndexed { index, can ->
                    val hoa = Constants.TU_HOA_MAP[index] ?: listOf("", "", "", "")
                    put(can, JSONObject().apply {
                        put("Lộc", hoa[0]); put("Quyền", hoa[1]); put("Khoa", hoa[2]); put("Kỵ", hoa[3])
                    })
                }
            })

            // Palaces (12 cung)
            put("palaces", buildPalacesJsonArray(cungList))

            // Fortune context: map viewing year to đại vận + overlap guidance
            put("fortune_context", JSONObject().apply {
                put("year", info.viewingYear)
                put("dai_van_current", info.daiVanInfo)
                put("tieu_han_cung", "${info.tieuHanCung} (năm ${info.viewingYear})")
<<<<<<< HEAD
=======
                if (info.luuNguyetCung.isNotEmpty()) {
                    put("luu_nguyet_cung", "${info.luuNguyetCung} (tháng ${info.viewingMonth})")
                }
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))
                put("tu_hoa_overlap_guide", "Phải kiểm tra xếp chồng tứ hóa: Song Kỵ (2 Kỵ cùng cung), Song Lộc (2 Lộc cùng cung), Lộc Kỵ giao nhau tại một cung. Ưu tiên phân tích: tứ hóa bản mệnh + đại vận xếp chồng → tác động lên Mệnh/Quan/Tài/Phu Thê trước, sau đó mới xét lưu niên.")
            })

            // Fortune request
            put("fortune_request", vanHanRequest)
        }
    }

    private fun buildTuHoaSummaryJson(cungList: List<CungInfo>): JSONObject {
        val result = JSONObject()

        // Helper to find cung containing a suffix
        fun findCungForSuffix(suffix: String): String? {
            val cung = cungList.find { c -> c.phuTinh.contains(suffix.trim()) }
            return cung?.let { "Cung ${it.name} (${it.chucNang})" }
        }

        // Ban Menh
        val bmObj = JSONObject()
        listOf("(Hóa Lộc)", "(Hóa Quyền)", "(Hóa Khoa)", "(Hóa Kỵ)").forEach { suffix ->
            findCungForSuffix(suffix)?.let { bmObj.put(suffix, it) }
        }
        result.put("ban_menh", bmObj)

        // Dai Van
        val dvObj = JSONObject()
        listOf("(ĐV. Hóa Lộc)", "(ĐV. Hóa Quyền)", "(ĐV. Hóa Khoa)", "(ĐV. Hóa Kỵ)").forEach { suffix ->
            findCungForSuffix(suffix)?.let { dvObj.put(suffix, it) }
        }
        result.put("dai_van", dvObj)

        // Luu Nien
        val lnObj = JSONObject()
        listOf("(L.Hóa Lộc)", "(L.Hóa Quyền)", "(L.Hóa Khoa)", "(L.Hóa Kỵ)").forEach { suffix ->
            findCungForSuffix(suffix)?.let { lnObj.put(suffix, it) }
        }
        result.put("luu_nien", lnObj)

        return result
    }

    private fun buildPalacesJsonArray(cungList: List<CungInfo>): JSONArray {
        val palaces = JSONArray()
        for (c in cungList) {
            val flags = JSONArray()
            if (c.chinhTinh.isEmpty()) flags.put("Vô chính diệu")
            if (c.phuTinh.any { it.startsWith("Tuần") }) flags.put("Gặp Tuần")
            if (c.phuTinh.any { it.startsWith("Triệt") }) flags.put("Gặp Triệt")

            val fixedPhu = c.phuTinh.filter {
                !it.startsWith("ĐV.") && !it.startsWith("L.") &&
                !it.startsWith("(ĐV.") && !it.startsWith("(L.") &&
                it != "[Cung Đại Vận]"
            }
            // Convert [Cung Đại Vận] marker to palace-level flag instead of empty-name star
            if (c.phuTinh.contains("[Cung Đại Vận]")) flags.put("Cung Đại Vận")
            val daiVanStars = c.phuTinh.filter { it.startsWith("ĐV.") || it.startsWith("(ĐV.") }
            val luuStars = c.phuTinh.filter { it.startsWith("L.") || it.startsWith("(L.") }

            val palace = JSONObject().apply {
                put("name", c.name)
                put("element", c.nguHanhCung)
                put("function", c.chucNang)
                if (flags.length() > 0) put("flags", flags)
                
                val starArr = JSONArray()
                c.chinhTinh.forEach { starArr.put(parseStarToJson(it, true)) }
                fixedPhu.forEach { starArr.put(parseStarToJson(it, false)) }
                put("fixed_stars", starArr)
                
                if (daiVanStars.isNotEmpty() || luuStars.isNotEmpty()) {
                    val transitArr = JSONArray()
                    (daiVanStars + luuStars).forEach { transitArr.put(parseStarToJson(it, false)) }
                    put("transit_stars", transitArr)
                }
            }
            palaces.put(palace)
        }
        return palaces
    }
    
    private val SAT_TINH = setOf("Kình Dương", "Đà La", "Hỏa Tinh", "Linh Tinh",
        "Địa Không", "Địa Kiếp", "Thiên Hình", "Kiếp Sát")

    private val CAT_TINH = setOf("Văn Xương", "Văn Khúc", "Tả Phù", "Hữu Bật",
        "Thiên Khôi", "Thiên Việt", "Lộc Tồn", "Thiên Mã", "Đào Hoa",
        "Hồng Loan", "Thiên Hỷ", "Long Trì", "Phượng Các", "Thiên Đức",
        "Nguyệt Đức", "Ân Quang", "Thiên Quý", "Thiên Quan", "Thiên Phúc",
        "Quốc Ấn", "Đường Phù", "Thai Phụ", "Phong Cáo", "Tam Thai",
        "Bát Tọa", "Thiên Giải", "Địa Giải", "Giải Thần")

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

    private fun parseDaiVanToJson(raw: String): JSONArray {
        val result = JSONArray()
        raw.split("|").map { it.trim() }.filter { it.isNotBlank() }.forEach { entry ->
            // Format: "5–14: Tân Sửu"
            val match = Regex("""^(\d+[–-]\d+):\s*(\S+)\s+(\S+)$""").find(entry.trim())
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

<<<<<<< HEAD
        val baseName = name.removePrefix("ĐV. ").removePrefix("L.")
=======
        val baseName = name.removePrefix("ĐV. ").removePrefix("L.").removePrefix("LN. ")
>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))

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

}
