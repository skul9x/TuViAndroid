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
            ReadingStyle.NGHIEM_TUC to "Phong cách luận giải: NGHIÊM TÚC, CỔ ĐIỂN, SÂU SẮC. Dùng từ ngữ chuyên môn nhưng giải thích dễ hiểu.\nXưng hô: 'Tại hạ' hoặc 'Tôi', gọi người xem là 'Đương số'.",
            ReadingStyle.DOI_THUONG to "Phong cách luận giải: ĐỜI THƯỜNG, DÂN DÃ, DỄ HIỂU. Dùng ví dụ thực tế, tránh lạm dụng từ Hán Việt.\nXưng hô: 'Tôi', gọi người xem là 'Bạn'.",
            ReadingStyle.HAI_HUOC to "Phong cách luận giải: HÀI HƯỚC, TRẺ TRUNG. Dùng slang, vui nhộn.\nXưng hô: 'Ad' hoặc 'Tui', gọi người xem là 'Bồ'.",
            ReadingStyle.KIEM_HIEP to "Phong cách luận giải: KIẾM HIỆP, CỔ TRANG. Dùng văn phong phim chưởng.\nXưng hô: 'Bần đạo' hoặc 'Lão phu', gọi người xem là 'Thí chủ'.",
            ReadingStyle.CHUA_LANH to "Phong cách luận giải: NHẸ NHÀNG, CHỮA LÀNH (HEALING). Khích lệ tinh thần.\nXưng hô: 'Mình', gọi người xem là 'Bạn'."
        )
        
        val selectedStylePrompt = stylePrompts[style] ?: stylePrompts[ReadingStyle.NGHIEM_TUC]!!

        val cungDetails = cungList.joinToString("\n") { c ->
            val starList = (c.chinhTinh + c.phuTinh).joinToString(", ")
            val specialContext = StringBuilder()
            if (c.phuTinh.contains("Tuần")) specialContext.append(" (Gặp Tuần)")
            if (c.phuTinh.contains("Triệt")) specialContext.append(" (Gặp Triệt)")
            
            // Note: Tứ Hóa already added to phuTinh list as separate entries like "(Hóa Lộc)", 
            // so they will appear in starList automatically.
            
            "- Cung ${c.name} (${c.chucNang})$specialContext: $starList"
        }

        return """
        Bạn là một chuyên gia TỬ VI ĐẨU SỐ hàng đầu.
        $selectedStylePrompt

        HÃY LUẬN GIẢI LÁ SỐ SAU ĐÂY:

        1. THÔNG TIN CƠ BẢN:
        - Đương số: ${info.name} (${info.gender})
        - Ngày sinh (Dương lịch): ${info.solarDate} lúc ${info.time}
        - Ngày sinh (Âm lịch): ${info.lunarDate} (${info.canChi})
        - Cục: ${info.cuc}
        - Mệnh đóng tại: ${info.menhTai}
        - Thân đóng tại: ${info.thanTai}
        - Năm xem hạn: ${info.viewingYear}

        2. CÁC CUNG VÀ SAO:
        $cungDetails

        YÊU CẦU LUẬN GIẢI CHI TIẾT CÁC VẤN ĐỀ SAU (Mỗi phần khoảng 100-150 chữ):
        1. Tổng quan về Mệnh và Thân (Tính cách, tố chất).
        2. Công danh, Sự nghiệp (Cung Quan Lộc).
        3. Tài lộc, tiền bạc (Cung Tài Bạch).
        4. Tình duyên, gia đạo (Cung Phu Thê).
        5. Lời khuyên tổng kết cho năm nay và tương lai.

        Hãy bình giải thật có tâm, dựa trên sự tương tác giữa các sao, thế đứng của các cung. Tuyệt đối không nói chung chung.
        """.trimIndent()
    }
}
