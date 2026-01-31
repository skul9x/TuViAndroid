package com.example.tviai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tviai.data.CungInfo
import com.example.tviai.ui.components.GoldButton
import com.example.tviai.viewmodel.TuViViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LasoScreen(
    viewModel: TuViViewModel,
    onNavigateToAnalysis: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val laso = uiState.currentLaso ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LÁ SỐ CHI TIẾT", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Traditional 12-palace grid (4x4)
            // Indices mapping to DIA_CHI: 0:Tý, 1:Sửu, 2:Dần...
            // Visual position in 4x4:
            // [5, 6, 7, 8]  (Tỵ, Ngọ, Mùi, Thân)
            // [4, C, C, 9]  (Thìn, Info, Info, Dậu)
            // [3, C, C, 10] (Mão, Info, Info, Tuất)
            // [2, 1, 0, 11] (Dần, Sửu, Tý, Hợi) - Reversed bottom
            
            Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                // Row 1 (Top)
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CungCell(laso.cung[5], Modifier.weight(1f))
                    CungCell(laso.cung[6], Modifier.weight(1f))
                    CungCell(laso.cung[7], Modifier.weight(1f))
                    CungCell(laso.cung[8], Modifier.weight(1f))
                }
                // Row 2
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CungCell(laso.cung[4], Modifier.weight(1f))
                    CentralInfo(laso, viewModel, onNavigateToAnalysis, Modifier.weight(2f))
                    CungCell(laso.cung[9], Modifier.weight(1f))
                }
                // Row 3 (Since central is 2x2, we jump row 3 center)
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CungCell(laso.cung[3], Modifier.weight(1f))
                    // Central info is 2x2, so we don't put anything here if CentralInfo handles it, 
                    // or we split CentralInfo. Let's make CentralInfo a single component 
                    // that covers middle 2x2.
                    Spacer(modifier = Modifier.weight(2f)) 
                    CungCell(laso.cung[10], Modifier.weight(1f))
                }
                // Row 4 (Bottom)
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CungCell(laso.cung[2], Modifier.weight(1f))
                    CungCell(laso.cung[1], Modifier.weight(1f))
                    CungCell(laso.cung[0], Modifier.weight(1f))
                    CungCell(laso.cung[11], Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CungCell(cung: CungInfo, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
            .padding(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Header: Function + Name
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(cung.chucNang, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(cung.name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Divider(modifier = Modifier.padding(vertical = 2.dp), thickness = 0.5.dp)

            // Stars
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                cung.chinhTinh.forEach { star ->
                    Text(star, color = Color(0xFFB71C1C), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                cung.phuTinh.forEach { star ->
                    Text(star, color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        
        // Score at bottom right
        Text(
            text = "${cung.score}",
            modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun CentralInfo(laso: com.example.tviai.data.LasoData, viewModel: TuViViewModel, onNavigate: () -> Unit, modifier: Modifier) {
    // This needs to span 2 rows height ideally, but for now we place in row 2 and wrap.
    // In a production layout, we'd use ConstraintLayout or a custom SubcomposeLayout for perfect 4x4 with center 2x2.
    // Here we overlap or use an absolute Box approach.
    
    // Quick fix: Just show key info in the assigned 2-col slot.
    Column(
        modifier = modifier.fillMaxHeight().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(laso.info.name.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        Text("${laso.info.gender} - ${laso.info.cuc}", fontSize = 12.sp)
        Text("Sinh: ${laso.info.lunarDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(laso.info.canChi, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        GoldButton(
            text = "Luận giải AI",
            onClick = {
                viewModel.generateAiReading()
                onNavigate()
            },
            modifier = Modifier.height(40.dp).fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Copy Prompt button
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        OutlinedButton(
            onClick = {
                val prompt = viewModel.getPrompt()
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(prompt))
            },
            modifier = Modifier.height(36.dp).fillMaxWidth(0.8f)
        ) {
            Text("📋 Copy Prompt", fontSize = 12.sp)
        }
    }
}
