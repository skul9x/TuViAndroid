package com.example.tviai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tviai.data.Gender
import com.example.tviai.data.ReadingStyle
import com.example.tviai.ui.components.GoldButton
import com.example.tviai.ui.components.PremiumCard
import com.example.tviai.ui.components.SectionHeader
import com.example.tviai.viewmodel.TuViViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    viewModel: TuViViewModel,
    onCalculate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "TViAI", 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(androidx.compose.material.icons.Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Nhập thông tin bản mệnh để AI luận giải bí mật vì sao của bạn.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Thông tin cơ bản
            PremiumCard {
                SectionHeader("Họ và tên")
                OutlinedTextField(
                    value = uiState.userInput.name,
                    onValueChange = { newValue ->
                        // Auto capitalize first letter of each word while typing
                        val titleCase = newValue.split(" ").joinToString(" ") { word ->
                            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                        viewModel.updateName(titleCase)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ví dụ: Nguyễn Văn An") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("Giới tính")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    GenderOption("Nam", uiState.userInput.gender == Gender.NAM) {
                        viewModel.updateGender(Gender.NAM)
                    }
                    GenderOption("Nữ", uiState.userInput.gender == Gender.NU) {
                        viewModel.updateGender(Gender.NU)
                    }
                }
            }

            // 2. Thời gian sinh
            PremiumCard {
                SectionHeader("Ngày tháng năm sinh (Dương lịch)")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DaySelector(
                        selectedDay = uiState.userInput.solarDay,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.updateBirthDate(it, uiState.userInput.solarMonth, uiState.userInput.solarYear) }
                    
                    MonthSelector(
                        selectedMonth = uiState.userInput.solarMonth,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.updateBirthDate(uiState.userInput.solarDay, it, uiState.userInput.solarYear) }
                    
                    BirthYearSelector(
                        selectedYear = uiState.userInput.solarYear,
                        modifier = Modifier.weight(1.2f)
                    ) { viewModel.updateBirthDate(uiState.userInput.solarDay, uiState.userInput.solarMonth, it) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("Giờ sinh")
                // Simplified hour selection
                HourSelector(selectedHour = uiState.userInput.hour) {
                    viewModel.updateHour(it)
                }
            }

            // 3. Năm xem hạn
            PremiumCard {
                SectionHeader("Năm xem hạn")
                Text(
                    "Chọn năm để luận giải vận hạn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                YearSelector(
                    selectedYear = uiState.userInput.viewingYear,
                    onYearSelected = { viewModel.updateViewingYear(it) }
                )
            }

            // 4. Phong cách luận giải
            PremiumCard {
                SectionHeader("Phong cách luận giải AI")
                ReadingStyleSelector(selectedStyle = uiState.userInput.readingStyle) {
                    viewModel.updateReadingStyle(it)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            GoldButton(
                text = "Xem Lá Số & Luận Giải",
                onClick = {
                    viewModel.calculateLaso()
                    onCalculate()
                },
                enabled = uiState.userInput.name.isNotBlank()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GenderOption(label: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        RadioButton(selected = isSelected, onClick = onSelect)
        Text(label, modifier = Modifier.clickable { onSelect() })
    }
}

@Composable
fun DateInput(label: String, value: String, modifier: Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourSelector(selectedHour: Int, onHourSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val hours = (0..23).toList()
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = "${selectedHour}h - Giờ ${com.example.tviai.core.Constants.DIA_CHI[((selectedHour + 1) % 24) / 2]}",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            hours.forEach { hour ->
                DropdownMenuItem(
                    text = { Text("${hour}h") },
                    onClick = {
                        onHourSelected(hour)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingStyleSelector(selectedStyle: ReadingStyle, onStyleSelected: (ReadingStyle) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedStyle.displayName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ReadingStyle.entries.forEach { style ->
                DropdownMenuItem(
                    text = { Text(style.displayName) },
                    onClick = {
                        onStyleSelected(style)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelector(selectedYear: Int, onYearSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYear - 10..currentYear + 10).toList()
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = "Năm $selectedYear",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = year.toString() + if (year == currentYear) " (Năm nay)" else "",
                            fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySelector(selectedDay: Int, modifier: Modifier = Modifier, onDaySelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val days = (1..31).toList()
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedDay.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Ngày", fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day.toString()) },
                    onClick = {
                        onDaySelected(day)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(selectedMonth: Int, modifier: Modifier = Modifier, onMonthSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val months = (1..12).toList()
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "Th$selectedMonth",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tháng", fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text("Tháng $month") },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthYearSelector(selectedYear: Int, modifier: Modifier = Modifier, onYearSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (1920..currentYear).toList().reversed() // Most recent first
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedYear.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Năm", fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}
