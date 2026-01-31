package com.example.tviai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tviai.core.GeminiClient
import com.example.tviai.core.TuViLogic
import com.example.tviai.data.Gender
import com.example.tviai.data.HistoryRepository
import com.example.tviai.data.LasoData
import com.example.tviai.data.ReadingStyle
import com.example.tviai.data.SettingsDataStore
import com.example.tviai.data.UserInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun calculateLaso() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = tuViLogic.anSao(_uiState.value.userInput)
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
