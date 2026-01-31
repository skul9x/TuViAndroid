package com.example.tviai.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tviai.core.GeminiClient
import com.example.tviai.data.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dataStore: SettingsDataStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State
    var apiKeysText by remember { mutableStateOf("") }
    var extractedKeys by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedModel by remember { mutableStateOf("gemini-2.0-flash") }
    var testResult by remember { mutableStateOf<Result<String>?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var showExtractionResult by remember { mutableStateOf(false) }

    // Load saved data
    LaunchedEffect(Unit) {
        extractedKeys = dataStore.apiKeys.first()
        selectedModel = dataStore.modelName.first()
    }

    // Model priority list (same as GeminiClient.MODEL_PRIORITY)
    val models = listOf(
        "Gemini 3 Flash Preview" to "gemini-3-flash-preview",
        "Gemini 2.5 Flash" to "gemini-2.5-flash",
        "Gemini 2.5 Flash Lite" to "gemini-2.5-flash-lite",
        "Gemini Flash Latest" to "gemini-flash-latest",
        "Gemini Flash Lite Latest" to "gemini-flash-lite-latest"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CÀI ĐẶT", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== API KEYS SECTION ==========
            Text("🔑 API Keys", style = MaterialTheme.typography.titleMedium)
            
            // Multi-line text field for pasting
            OutlinedTextField(
                value = apiKeysText,
                onValueChange = { apiKeysText = it },
                label = { Text("Paste API Keys vào đây (có thể paste nhiều)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                placeholder = { Text("App sẽ tự động trích xuất các key bắt đầu bằng AIza...") }
            )

            // Paste & Extract button
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Paste from clipboard button
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            apiKeysText = clip.getItemAt(0).text?.toString() ?: ""
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Paste từ Clipboard")
                }

                // Extract button
                Button(
                    onClick = {
                        scope.launch {
                            val keys = dataStore.extractAndSaveApiKeys(apiKeysText)
                            extractedKeys = keys
                            showExtractionResult = true
                            apiKeysText = "" // Clear after extraction
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = apiKeysText.isNotBlank()
                ) {
                    Text("Trích xuất Keys")
                }
            }

            // Show extraction result
            if (showExtractionResult) {
                if (extractedKeys.isNotEmpty()) {
                    Text(
                        "✅ Đã trích xuất ${extractedKeys.size} API Key(s)",
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Text(
                        "❌ Không tìm thấy API Key nào (phải bắt đầu bằng AIza...)",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Display extracted keys
            if (extractedKeys.isNotEmpty()) {
                Text("📋 Danh sách API Keys đã lưu:", style = MaterialTheme.typography.labelLarge)
                
                extractedKeys.forEachIndexed { index, key ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == 0) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (index == 0) "🌟 Key ${index + 1} (Đang dùng)" else "Key ${index + 1}",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = maskApiKey(key),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val newKeys = extractedKeys.toMutableList()
                                        newKeys.removeAt(index)
                                        dataStore.saveApiKeys(newKeys)
                                        extractedKeys = newKeys
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Xóa",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ========== MODEL SELECTION ==========
            Text("🤖 Chọn Model AI", style = MaterialTheme.typography.titleMedium)
            Text(
                "Ưu tiên từ trên xuống dưới (model xịn nhất ở trên)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            models.forEachIndexed { index, (displayName, modelId) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedModel = modelId }
                        .background(
                            if (selectedModel == modelId) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                            else 
                                Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedModel == modelId,
                        onClick = { selectedModel = modelId }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedModel == modelId) FontWeight.Bold else FontWeight.Normal
                        )
                        if (index < 3) {
                            Text(
                                text = when (index) {
                                    0 -> "⭐ Suy luận sâu nhất"
                                    1 -> "🚀 Mạnh mẽ, đa năng"
                                    2 -> "🧪 Thử nghiệm, mới nhất"
                                    else -> ""
                                },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ========== TEST CONNECTION ==========
            Text("🔗 Kiểm tra kết nối", style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = {
                    isTesting = true
                    testResult = null
                    scope.launch {
                        val keyToTest = extractedKeys.firstOrNull() ?: ""
                        if (keyToTest.isBlank()) {
                            testResult = Result.failure(Exception("Chưa có API Key nào được lưu"))
                        } else {
                            testResult = GeminiClient.testConnection(keyToTest, selectedModel)
                        }
                        isTesting = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTesting && extractedKeys.isNotEmpty()
            ) {
                if (isTesting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đang kiểm tra...")
                } else {
                    Text("Kiểm tra kết nối với ${models.find { it.second == selectedModel }?.first ?: selectedModel}")
                }
            }

            testResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isSuccess) 
                            Color(0xFFE8F5E9) 
                        else 
                            Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (result.isSuccess) 
                            "✅ ${result.getOrNull()}" 
                        else 
                            "❌ ${result.exceptionOrNull()?.message}",
                        modifier = Modifier.padding(12.dp),
                        color = if (result.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== SAVE BUTTON ==========
            Button(
                onClick = {
                    scope.launch {
                        dataStore.saveModelName(selectedModel)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("💾 LƯU CẤU HÌNH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Mask API key for display: AIza...xxxxx
 */
private fun maskApiKey(key: String): String {
    return if (key.length > 10) {
        "${key.take(4)}...${key.takeLast(5)}"
    } else {
        key
    }
}
