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
import com.example.tviai.data.remote.TelemetryRepository
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val telemetryRepository: TelemetryRepository,
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

    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(userInput = it.userInput.copy(phoneNumber = phone)) }
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
            val newInput = if (mode == ViewingMode.MONTH && state.userInput.viewingMonth == 0) {
                // First time switching to MONTH -> set default to next month
                val cal = Calendar.getInstance()
                val nextMonth = cal.get(Calendar.MONTH) + 2 // Calendar.MONTH is 0-based, +1 for real, +1 for next
                val (month, year) = if (nextMonth > 12) {
                    1 to state.userInput.viewingYear + 1
                } else {
                    nextMonth to state.userInput.viewingYear
                }
                state.userInput.copy(viewingMode = mode, viewingMonth = month, viewingYear = year)
            } else {
                state.userInput.copy(viewingMode = mode)
            }
            state.copy(userInput = newInput)
        }
    }

    fun updateViewingMonth(month: Int) {
        _uiState.update { it.copy(userInput = it.userInput.copy(viewingMonth = month)) }
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
                
                // SYNC TO SUPABASE (Background - Fire and Forget)
                viewModelScope.launch(Dispatchers.IO) {
                    telemetryRepository.syncLasoData(result)
                }
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

    fun setLaso(laso: LasoData) {
        _uiState.update { it.copy(currentLaso = laso, aiReading = "", usedModel = "") }
    }

    fun getPrompt(): String {
        val laso = _uiState.value.currentLaso ?: return ""
        return geminiClient?.getPromptForCopy(laso) ?: ""
    }

    fun setUsedModel(modelName: String) {
        _uiState.update { it.copy(usedModel = modelName) }
    }
}
