package com.example.tviai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tviai.data.HistoryEntity
import com.example.tviai.data.HistoryRepository
import com.example.tviai.data.LasoData
import com.example.tviai.ui.components.PremiumCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: HistoryRepository,
    onSelect: (LasoData) -> Unit,
    onBack: () -> Unit
) {
    val historyList by repository.getAllHistory().collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("LỊCH SỬ LÁ SỐ", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Chưa có lịch sử tính toán nào.")
            }
        } else {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList) { item ->
                    PremiumCard(
                        modifier = Modifier.clickable {
                            scope.launch {
                                repository.getLasoDetail(item.id)?.let { onSelect(it) }
                            }
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${item.birthDate} - ${item.birthTime}", fontSize = 12.sp, color = Color.Gray)
                                Text("Cục: ${item.cuc}", fontSize = 12.sp)
                            }
                            Text(
                                dateFormat.format(Date(item.timestamp)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
