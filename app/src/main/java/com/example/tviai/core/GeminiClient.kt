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

        val cungDetails = cungList.joinToString("\n") { c ->
            val starList = (c.chinhTinh + c.phuTinh).joinToString(", ")
            val voChinhDieu = if (c.chinhTinh.isEmpty()) " [Vô chính diệu]" else ""
            val specialContext = StringBuilder()
            if (c.phuTinh.contains("Tuần")) specialContext.append(" (Gặp Tuần)")
            if (c.phuTinh.contains("Triệt")) specialContext.append(" (Gặp Triệt)")
            
            "- Cung ${c.name} [${c.nguHanhCung}] (${c.chucNang})$voChinhDieu$specialContext: $starList"
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
            "Phân tích vận tháng ${info.viewingMonth} âm lịch năm ${info.viewingYear} (theo đại vận + tiểu vận + lưu thái tuế + lưu hóa tinh nếu có dữ liệu)"
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
        Nếu thiếu thông tin quan trọng → hỏi lại tối đa 3 câu.

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

        8. ⛔ KHÔNG ĐƯỢC tự tính toán bất kỳ dữ liệu nào.
        CHỈ sử dụng dữ liệu đã được cung cấp sẵn bên dưới.
        Cụ thể KHÔNG ĐƯỢC:
        • Tự tính miếu / vượng / đắc / hãm (dữ liệu đã có sẵn ký hiệu M/V/Đ/H sau tên chính tinh)
        • Tự xác định đại vận (thông tin đại vận đã được cung cấp đầy đủ)
        • Tự tính lưu tinh hay lưu tứ hóa (dữ liệu đã có prefix L. và ĐV.)
        • Tự suy ra cách cục không dựa trên sao thực tế trong lá số
        Nếu dữ liệu không có → ghi rõ "Không có trong dữ liệu được cung cấp".
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
        7 PHƯƠNG PHÁP LUẬN BẮT BUỘC (LEVEL 5)

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
                result.add("$label (${Constants.DIA_CHI[nhatIdx]}–${Constants.DIA_CHI[nguyetIdx]})")
            }
        }

        return result
    }
}
