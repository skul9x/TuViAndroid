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

        return """
        Bạn là một nhà mệnh lý học chuyên sâu về TỬ VI ĐẨU SỐ, có khả năng phân tích tinh hệ ở mức cấu trúc – không luận theo cảm tính.

        MỤC TIÊU:
        Luận giải lá số dựa trên hệ thống sao – cung – vận – ngũ hành một cách logic, có dẫn chứng tinh hệ cụ thể cho từng nhận định.

        PHONG CÁCH:
        - $selectedStylePrompt
        - Không viết truyền cảm hứng.
        - Không phán định tuyệt đối.
        - Không nói chung chung kiểu tâm lý học.
        - Mọi kết luận phải có căn cứ sao cụ thể.

        ==================================================
        NGUYÊN TẮC BẮT BUỘC
        ==================================================

        1. Phải xác định rõ:
           - Mệnh đóng ở đâu, thuộc hành gì.
           - Cục gì, sinh khắc giữa Mệnh và Cục.
           - Thân cư cung nào, Mệnh – Thân đồng cung hay phân cung.
           - Âm dương thuận nghịch lý số.

        2. Trong mỗi cung khi luận phải xét đầy đủ:
           - Chính tinh (miếu, vượng, đắc, hãm nếu có)
           - Phụ tinh trọng yếu (cát tinh, sát tinh)
           - Tam hợp, Xung chiếu, Giáp cung
           - Tuần / Triệt
           - Hóa Lộc – Hóa Quyền – Hóa Khoa – Hóa Kỵ

        3. Nếu xuất hiện cách cục đặc biệt phải chỉ rõ:
           - Sát Phá Tham, Cơ Nguyệt Đồng Lương, Nhật Nguyệt chiếu mệnh, Phủ Tướng triều viên, Hoặc các cách cục đặc biệt khác.

        4. Phải xác định: Đương số đang ở đại vận nào, kích hoạt mạnh nhất cung nào.

        5. Khi luận vận: Xét đại vận, tiểu vận, lưu thái tuế.

        ==================================================
        NHỮNG ĐIỀU KHÔNG ĐƯỢC LÀM
        ==================================================
        - Không khẳng định tử vong, tai nạn nghiêm trọng, bệnh hiểm nghèo.
        - Không đoán chính xác số lượng con cái. Không gán chỉ số IQ cụ thể.
        - Không suy diễn tâm linh dòng họ nếu tinh hệ không thể hiện rõ.
        - Không nói “số đã định không thay đổi”.

        ==================================================
        LÁ SỐ CỦA ĐƯƠNG SỐ:
        
        1. THÔNG TIN CƠ BẢN:
        - Đương số: ${info.name} (${info.gender})
        - Ngày sinh (Dương lịch): ${info.solarDate} lúc ${info.time}
        - Ngày sinh (Âm lịch): ${info.lunarDate} (${info.canChi})
        - Cục: ${info.cuc}
        - Mệnh đóng tại: ${info.menhTai}
        - Thân đóng tại: ${info.thanTai}
        - Khoảng thời gian xem vận: ${if (info.viewingMode == "MONTH") "Tháng ${info.viewingMonth} năm ${info.viewingYear}" else "Năm ${info.viewingYear}"}

        2. CÁC CUNG VÀ SAO:
        $cungDetails
        
        ==================================================
        YÊU CẦU CẤU TRÚC LUẬN (Bắt buộc theo thứ tự này):

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

        Mỗi cung phải theo cấu trúc: 1. Chính tinh, 2. Phụ tinh, 3. Tam hợp/Xung chiếu/Giáp cung, 4. Tuần/Triệt, 5. Hóa tinh, 6. Tổng hợp.

        ==================================================
        PHẦN TỔNG KẾT BẮT BUỘC:
        - Tổng quan mệnh cách (ổn định / biến động / thành muộn / đa truân...)
        - Điểm mạnh nổi bật nhất (dẫn chứng sao)
        - Điểm dễ tự làm khó mình (dẫn chứng sao)
        - Hướng tu dưỡng thực tế phù hợp mệnh cách
        - $vanHanRequest

        Hãy bình giải thật có tâm, dựa trên sự tương tác của các tinh hệ.
        """.trimIndent()
    }
}
