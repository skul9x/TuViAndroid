package com.example.tviai.core

import com.example.tviai.data.LasoData
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
            val specialContext = StringBuilder()
            if (c.phuTinh.contains("Tuần")) specialContext.append(" (Gặp Tuần)")
            if (c.phuTinh.contains("Triệt")) specialContext.append(" (Gặp Triệt)")
            
            "- Cung ${c.name} (${c.chucNang})$specialContext: $starList"
        }

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
        QUY TRÌNH PHÂN TÍCH BẮT BUỘC

        Trước khi luận chi tiết phải thực hiện 3 bước.

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

        Nếu phát hiện:

        Phải ghi rõ:

        • tên cách cục
        • sao tạo cách
        • vị trí cung
        • điều kiện đạt cách
        • có sát tinh phá cách không

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
        • lưu ý vận hạn

        =====================================

        QUY ƯỚC KÝ HIỆU TRONG DỮ LIỆU:
        • (M) = Miếu, (V) = Vượng, (Đ) = Đắc, (H) = Hãm — trạng thái của chính tinh
        • (Hóa Lộc), (Hóa Quyền), (Hóa Khoa), (Hóa Kỵ) — Tứ hóa bản mệnh
        • ĐV. = Sao Đại Vận (VD: ĐV. Lộc Tồn, ĐV. H Lộc = Đại Vận Hóa Lộc)
        • L. = Sao Lưu niên (VD: L.Kình Dương, L.Hóa Kỵ = Lưu niên Hóa Kỵ)
        • Tuần, Triệt = Tuần Không và Triệt Không (sao bị Tuần/Triệt sẽ giảm lực)
        • Cung không có chính tinh = Vô chính diệu → xem chính tinh cung đối chiếu (xung chiếu) để luận

        Nội dung lá số:

        $lasoContent
        """.trimIndent()
    }
}
