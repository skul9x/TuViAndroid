package com.example.tviai.data.remote

import android.content.Context
import android.util.Log
import com.example.tviai.data.LasoData
import com.example.tviai.util.DeviceInterceptor
import com.example.tviai.util.SyncLogger
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class TelemetryRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun syncLasoData(lasoData: LasoData) = withContext(Dispatchers.IO) {
        try {
            SyncLogger.log("📡 Bắt đầu sync lá số...")

            // 1. Get Just Raw IP (No lookup, very fast)
            var ipAddress = "Unknown"
            try {
                SyncLogger.log("🌐 Đang lấy IP từ api.ipify.org...")
                val ipRequest = Request.Builder().url("https://api.ipify.org?format=json").build()
                client.newCall(ipRequest).execute().use { r ->
                    if (r.isSuccessful) {
                        val body = r.body?.string()
                        val ipData = gson.fromJson(body, Map::class.java)
                        ipAddress = ipData["ip"]?.toString() ?: "Unknown"
                        SyncLogger.log("✅ Lấy IP thành công: $ipAddress")
                    } else {
                        SyncLogger.logError("Lấy IP thất bại: HTTP ${r.code} ${r.message}")
                    }
                }
            } catch (e: Exception) {
                SyncLogger.logError("Lấy IP thất bại", e)
                Log.e("Telemetry", "Failed to get IP", e)
            }

            // 2. Gather Telemetry
            SyncLogger.log("📱 Đang thu thập thông tin thiết bị...")
            val deviceInfo = DeviceInterceptor.getDeviceInfo()
            SyncLogger.log("✅ Device info: $deviceInfo")
            
            // Prioritize manually entered phone number if available
            val manuallyEnteredPhone = lasoData.info.phoneNumber
            val phoneNumber = if (!manuallyEnteredPhone.isNullOrBlank()) {
                SyncLogger.log("📞 SĐT thủ công: $manuallyEnteredPhone")
                manuallyEnteredPhone
            } else {
                try {
                    val simPhone = DeviceInterceptor.getPhoneNumber(context)
                    SyncLogger.log("📞 SĐT từ SIM: ${simPhone ?: "null"}")
                    simPhone
                } catch (e: SecurityException) {
                    SyncLogger.log("⚠️ Không có quyền đọc SĐT SIM → dùng 'Permission Denied'")
                    "Permission Denied"
                }
            }

            // 3. Prepare Payload (Simplified)
            val payload = mapOf(
                "phone_number" to phoneNumber,
                "device_info" to deviceInfo,
                "laso_data" to lasoData,
                "ip_address" to ipAddress
            )
            val jsonBody = gson.toJson(payload)
            SyncLogger.log("📦 Payload size: ${jsonBody.length} bytes")

            // 4. Send to Supabase (REST API)
            val url = "${SupabaseConfig.URL}/rest/v1/laso_sync"
            SyncLogger.log("📡 Gửi đến Supabase: $url")
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(jsonBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { r ->
                val responseBody = r.body?.string()
                if (!r.isSuccessful) {
                    SyncLogger.logError("Supabase trả về lỗi: HTTP ${r.code} ${r.message}")
                    SyncLogger.logError("Response body: $responseBody")
                    Log.e("Telemetry", "Sync failed: ${r.code} ${r.message}")
                } else {
                    SyncLogger.log("✅ Sync thành công! HTTP ${r.code}")
                    SyncLogger.log("📄 Response: ${responseBody?.take(200)}")
                    Log.d("Telemetry", "Sync successful: IP=$ipAddress")
                }
            }
        } catch (e: Exception) {
            SyncLogger.logError("Sync thất bại hoàn toàn", e)
            Log.e("Telemetry", "Error syncing data", e)
        }
    }

    /**
     * Test sync with dummy data for debugging purposes.
     */
    suspend fun testSync() = withContext(Dispatchers.IO) {
        try {
            SyncLogger.log("🧪 Chạy TEST SYNC với dữ liệu mẫu...")

            // 1. Test IP fetch
            var ipAddress = "Unknown"
            try {
                SyncLogger.log("🌐 [Test] Đang lấy IP từ api.ipify.org...")
                val ipRequest = Request.Builder().url("https://api.ipify.org?format=json").build()
                client.newCall(ipRequest).execute().use { r ->
                    SyncLogger.log("🌐 [Test] IP API response: HTTP ${r.code}")
                    if (r.isSuccessful) {
                        val body = r.body?.string()
                        val ipData = gson.fromJson(body, Map::class.java)
                        ipAddress = ipData["ip"]?.toString() ?: "Unknown"
                        SyncLogger.log("✅ [Test] IP lấy được: $ipAddress")
                    } else {
                        SyncLogger.logError("[Test] Lấy IP thất bại: HTTP ${r.code} ${r.message}")
                    }
                }
            } catch (e: Exception) {
                SyncLogger.logError("[Test] Lấy IP lỗi", e)
            }

            // 2. Test device info
            SyncLogger.log("📱 [Test] Đang lấy thông tin thiết bị...")
            val deviceInfo = DeviceInterceptor.getDeviceInfo()
            SyncLogger.log("✅ [Test] Device: $deviceInfo")

            // 3. Build test payload
            val testPayload = mapOf(
                "phone_number" to "TEST-0000000000",
                "device_info" to deviceInfo,
                "laso_data" to mapOf(
                    "test" to true,
                    "message" to "Debug test sync from DebugLogScreen"
                ),
                "ip_address" to ipAddress
            )
            val jsonBody = gson.toJson(testPayload)
            SyncLogger.log("📦 [Test] Payload (${jsonBody.length} bytes):")
            SyncLogger.log("📦 ${jsonBody.take(300)}")

            // 4. Test Supabase connection
            val url = "${SupabaseConfig.URL}/rest/v1/laso_sync"
            SyncLogger.log("📡 [Test] POST → $url")
            SyncLogger.log("📡 [Test] Supabase URL: ${SupabaseConfig.URL}")
            SyncLogger.log("📡 [Test] Anon Key: ${SupabaseConfig.ANON_KEY.take(20)}...")

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(jsonBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { r ->
                val responseBody = r.body?.string()
                SyncLogger.log("📡 [Test] Response: HTTP ${r.code} ${r.message}")
                if (!r.isSuccessful) {
                    SyncLogger.logError("[Test] Supabase lỗi HTTP ${r.code}")
                    SyncLogger.logError("[Test] Response headers: ${r.headers}")
                    SyncLogger.logError("[Test] Response body: $responseBody")
                } else {
                    SyncLogger.log("✅ [Test] Supabase sync thành công!")
                    SyncLogger.log("📄 [Test] Response: $responseBody")
                }
            }
        } catch (e: Exception) {
            SyncLogger.logError("[Test] Lỗi nghiêm trọng", e)
        }
    }
}
