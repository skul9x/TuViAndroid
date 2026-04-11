package com.example.tviai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tviai.core.GeminiClient
import com.example.tviai.core.TuViLogic
import com.example.tviai.data.Gender
import com.example.tviai.data.HistoryRepository
import com.example.tviai.data.LasoData
import com.example.tviai.data.ReadingStyle
import com.example.tviai.data.ViewingMode
import com.example.tviai.data.SettingsDataStore
import com.example.tviai.data.UserInput
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TuViUiState(
    val userInput: UserInput = UserInput(
        name = "",
        solarDay = 1,
        solarMonth = 1,
        solarYear = 1990,
        hour = 12,
        gender = Gender.NAM,
        viewingYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
        readingStyle = ReadingStyle.NGHIEM_TUC
    ),
    val currentLaso: LasoData? = null,
    val aiReading: String = "",
    val usedModel: String = "",
    val isGeneratingAi: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TuViViewModel(
    private val historyRepository: HistoryRepository,
    private val settingsDataStore: SettingsDataStore,
    private val tuViLogic: TuViLogic = TuViLogic()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TuViUiState())
    val uiState: StateFlow<TuViUiState> = _uiState.asStateFlow()

    private var geminiClient: GeminiClient? = null

    init {
        // Observe settings to initialize GeminiClient with multi-key support
        viewModelScope.launch {
            settingsDataStore.apiKeys.collectLatest { keys ->
                settingsDataStore.modelName.collectLatest { modelName ->
                    if (keys.isNotEmpty()) {
                        if (geminiClient == null) {
                            geminiClient = GeminiClient(
                                apiKeys = keys,
                                modelName = modelName,
                                settingsDataStore = settingsDataStore
                            )
                        } else {
                            geminiClient?.updateConfig(keys, modelName)
                        }
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(userInput = it.userInput.copy(name = name)) }
    }

    fun updateBirthDate(day: Int, month: Int, year: Int) {
        _uiState.update { 
            it.copy(userInput = it.userInput.copy(solarDay = day, solarMonth = month, solarYear = year)) 
        }
    }

    fun updateHour(hour: Int) {
        _uiState.update { it.copy(userInput = it.userInput.copy(hour = hour)) }
    }

    fun updateGender(gender: Gender) {
        _uiState.update { it.copy(userInput = it.userInput.copy(gender = gender)) }
    }
    
    fun updateReadingStyle(style: ReadingStyle) {
        _uiState.update { it.copy(userInput = it.userInput.copy(readingStyle = style)) }
    }

    fun updateViewingYear(year: Int) {
        _uiState.update { it.copy(userInput = it.userInput.copy(viewingYear = year)) }
    }

    fun updateViewingMode(mode: ViewingMode) {
        _uiState.update { state ->
            val newInput = when {
                mode == ViewingMode.MONTH && state.userInput.viewingMonth == 0 -> {
                    // First time switching to MONTH -> set default to current lunar month
                    val cal = Calendar.getInstance()
                    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                    val currentMonth = cal.get(Calendar.MONTH) + 1
                    val currentYear = cal.get(Calendar.YEAR)
                    val lunarDate = com.example.tviai.core.LunarConverter.convertSolarToLunar(currentDay, currentMonth, currentYear)
                    state.userInput.copy(viewingMode = mode, viewingMonth = lunarDate.month, viewingYear = lunarDate.year)
                }
                mode == ViewingMode.DAY && state.userInput.viewingDay == 0 -> {
                    // First time switching to DAY -> set default to current lunar day/month
                    val cal = Calendar.getInstance()
                    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                    val currentMonth = cal.get(Calendar.MONTH) + 1
                    val currentYear = cal.get(Calendar.YEAR)
                    val lunarDate = com.example.tviai.core.LunarConverter.convertSolarToLunar(currentDay, currentMonth, currentYear)
                    state.userInput.copy(
                        viewingMode = mode, 
                        viewingDay = lunarDate.day, 
                        viewingMonth = lunarDate.month, 
                        viewingYear = lunarDate.year
                    )
                }
                else -> state.userInput.copy(viewingMode = mode)
            }
            state.copy(userInput = newInput)
        }
    }

    fun updateViewingMonth(month: Int) {
        _uiState.update { it.copy(userInput = it.userInput.copy(viewingMonth = month)) }
    }

    fun updateViewingDay(day: Int) {
        _uiState.update { it.copy(userInput = it.userInput.copy(viewingDay = day)) }
    }

    fun calculateLaso() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.Default) {
                    tuViLogic.anSao(_uiState.value.userInput)
                }
                _uiState.update { it.copy(currentLaso = result, isLoading = false) }
                // Save to history automatically
                historyRepository.saveLaso(result)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tính toán: ${e.message}") }
            }
        }
    }

    fun generateAiReading() {
        val laso = _uiState.value.currentLaso ?: return
        val client = geminiClient
        if (client == null) {
            _uiState.update { it.copy(error = "Chưa cấu hình API Key trong Cài đặt") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAi = true, aiReading = "", usedModel = "") }
            client.generateReadingStream(laso).collect { chunk ->
                _uiState.update { it.copy(aiReading = it.aiReading + chunk) }
            }
            // Set the model name after generation completes
            _uiState.update { 
                it.copy(
                    isGeneratingAi = false, 
                    usedModel = client.getModelName()
                ) 
            }
        }
    }

    // Event channel for one-time UI events (e.g., Toast)
    private val _historyLoadedEvent = MutableSharedFlow<String>()
    val historyLoadedEvent: SharedFlow<String> = _historyLoadedEvent.asSharedFlow()

    fun setLaso(laso: LasoData) {
        _uiState.update { it.copy(currentLaso = laso, aiReading = "", usedModel = "") }
    }

    /**
     * Load history data back into InputScreen fields.
     * Parses solarDate, time, gender, readingStyle from UserInfoResult.
     * Resets viewing mode to defaults (current year, YEAR mode).
     */
    fun loadFromHistory(laso: LasoData) {
        val info = laso.info

        // Parse solarDate "day/month/year" → individual ints
        val dateParts = info.solarDate.split("/")
        val day = dateParts.getOrNull(0)?.toIntOrNull() ?: 1
        val month = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
        val year = dateParts.getOrNull(2)?.toIntOrNull() ?: 1990

        // Parse time "0h (Giờ Tý)" → hour int
        val hour = parseHourFromTimeString(info.time)

        // Parse gender
        val gender = if (info.gender == "Nữ") Gender.NU else Gender.NAM

        // Parse reading style
        val readingStyle = ReadingStyle.fromString(info.readingStyle)

        _uiState.update {
            it.copy(
                userInput = UserInput(
                    name = info.name,
                    solarDay = day,
                    solarMonth = month,
                    solarYear = year,
                    hour = hour,
                    gender = gender,
                    viewingYear = Calendar.getInstance().get(Calendar.YEAR),
                    readingStyle = readingStyle
                ),
                currentLaso = null,
                aiReading = "",
                usedModel = ""
            )
        }

        // Fire one-time event for Toast
        viewModelScope.launch {
            _historyLoadedEvent.emit("Đã tải thông tin từ lịch sử")
        }
    }

    /**
     * Parse hour from time string format: "0h (Giờ Tý)" or similar.
     * First tries to extract the number before 'h', then falls back to Chi name mapping.
     */
    private fun parseHourFromTimeString(time: String): Int {
        // Try to extract hour number directly: "0h (Giờ Tý)" → "0"
        val hourMatch = Regex("""^(\d+)h""").find(time)
        if (hourMatch != null) {
            return hourMatch.groupValues[1].toIntOrNull() ?: 12
        }

        // Fallback: map Chi name to hour
        val chiToHour = mapOf(
            "Tý" to 0, "Sửu" to 2, "Dần" to 4, "Mão" to 6,
            "Thìn" to 8, "Tị" to 10, "Ngọ" to 12, "Mùi" to 14,
            "Thân" to 16, "Dậu" to 18, "Tuất" to 20, "Hợi" to 22
        )
        return chiToHour.entries.firstOrNull { time.contains(it.key) }?.value ?: 12
    }

    fun resetInput() {
        _uiState.update { 
            it.copy(
                userInput = UserInput(
                    name = "",
                    solarDay = 1,
                    solarMonth = 1,
                    solarYear = 1990,
                    hour = 12,
                    gender = Gender.NAM,
                    viewingYear = Calendar.getInstance().get(Calendar.YEAR),
                    readingStyle = ReadingStyle.NGHIEM_TUC
                )
            ) 
        }
    }

    fun getPrompt(): String {
        val laso = _uiState.value.currentLaso ?: return ""
        return geminiClient?.getPromptForCopy(laso) ?: ""
    }

    fun setUsedModel(modelName: String) {
        _uiState.update { it.copy(usedModel = modelName) }
    }
}
