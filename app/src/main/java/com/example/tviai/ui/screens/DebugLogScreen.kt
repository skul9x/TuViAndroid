package com.example.tviai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tviai.data.remote.TelemetryRepository
import com.example.tviai.util.SyncLogger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by SyncLogger.logs.collectAsState()
    val listState = rememberLazyListState()
    var isSyncing by remember { mutableStateOf(false) }

    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐛 Debug Sync", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Copy button
                    IconButton(
                        onClick = {
                            val text = SyncLogger.getAllLogsAsText()
                            if (text.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Sync Debug Log", text))
                                Toast.makeText(context, "✅ Đã copy log!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Chưa có log nào", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = logs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Log")
                    }
                    // Share button
                    IconButton(
                        onClick = {
                            val text = SyncLogger.getAllLogsAsText()
                            if (text.isNotBlank()) {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "=== TuVi Sync Debug Log ===\n$text")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ Debug Log"))
                            }
                        },
                        enabled = logs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Log")
                    }
                    // Clear button
                    IconButton(
                        onClick = { SyncLogger.clear() },
                        enabled = logs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Log", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Action bar: Test Sync button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Test Supabase Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Gửi một bản ghi test lên Supabase",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            isSyncing = true
                            scope.launch {
                                try {
                                    SyncLogger.log("🚀 === BẮT ĐẦU TEST SYNC ===")
                                    val repo = TelemetryRepository(context)
                                    repo.testSync()
                                    SyncLogger.log("✅ === KẾT THÚC TEST SYNC ===")
                                } catch (e: Exception) {
                                    SyncLogger.logError("Test sync thất bại hoàn toàn", e)
                                } finally {
                                    isSyncing = false
                                }
                            }
                        },
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Đang sync...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Test Sync")
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            // Log console
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Chưa có log nào",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Bấm \"Test Sync\" để bắt đầu debug",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(logs) { logEntry ->
                        val textColor = when {
                            logEntry.contains("❌ ERROR") -> Color(0xFFFF6B6B)
                            logEntry.contains("✅") -> Color(0xFF51CF66)
                            logEntry.contains("⚠️") -> Color(0xFFFFD43B)
                            logEntry.contains("🚀") -> Color(0xFF74C0FC)
                            logEntry.contains("📡") -> Color(0xFFB197FC)
                            else -> Color(0xFFDEE2E6)
                        }

                        Text(
                            text = logEntry,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = textColor,
                            lineHeight = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (logEntry.contains("ERROR")) Color(0xFF2D1B1B)
                                    else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
