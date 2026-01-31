package com.example.tviai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        // Legacy single key (kept for migration)
        val API_KEY = stringPreferencesKey("gemini_api_key")
        
        // New multi-key storage (JSON array string)
        val API_KEYS_JSON = stringPreferencesKey("api_keys_json")
        val ACTIVE_KEY_INDEX = intPreferencesKey("active_key_index")
        
        val MODEL_NAME = stringPreferencesKey("model_name")
        val READING_STYLE = stringPreferencesKey("reading_style")
        
        // Regex pattern for Gemini API Key
        val API_KEY_REGEX = Regex("AIza[0-9A-Za-z_-]{35}")
    }

    // ========== API KEYS (Multi-key support) ==========
    
    val apiKeys: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[API_KEYS_JSON] ?: ""
            if (json.isBlank()) {
                // Migration: Check if old single key exists
                val legacyKey = preferences[API_KEY] ?: ""
                if (legacyKey.isNotBlank()) listOf(legacyKey) else emptyList()
            } else {
                json.split(",").filter { it.isNotBlank() }
            }
        }

    val activeKeyIndex: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[ACTIVE_KEY_INDEX] ?: 0
        }

    /**
     * Get the currently active API key
     */
    val currentApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            val json = preferences[API_KEYS_JSON] ?: ""
            val keys = if (json.isBlank()) {
                val legacyKey = preferences[API_KEY] ?: ""
                if (legacyKey.isNotBlank()) listOf(legacyKey) else emptyList()
            } else {
                json.split(",").filter { it.isNotBlank() }
            }
            val index = preferences[ACTIVE_KEY_INDEX] ?: 0
            keys.getOrElse(index) { keys.firstOrNull() ?: "" }
        }

    /**
     * Extract API keys from text using regex and save them
     */
    suspend fun extractAndSaveApiKeys(text: String): List<String> {
        val keys = API_KEY_REGEX.findAll(text)
            .map { it.value }
            .distinct()
            .toList()
        
        if (keys.isNotEmpty()) {
            context.dataStore.edit { preferences ->
                preferences[API_KEYS_JSON] = keys.joinToString(",")
                preferences[ACTIVE_KEY_INDEX] = 0
            }
        }
        return keys
    }

    /**
     * Save a list of API keys directly
     */
    suspend fun saveApiKeys(keys: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[API_KEYS_JSON] = keys.joinToString(",")
            preferences[ACTIVE_KEY_INDEX] = 0
        }
    }

    /**
     * Rotate to the next API key. Returns true if there's another key, false if exhausted.
     */
    suspend fun rotateToNextKey(): Boolean {
        val keys = apiKeys.first()
        val currentIndex = activeKeyIndex.first()
        
        return if (currentIndex + 1 < keys.size) {
            context.dataStore.edit { preferences ->
                preferences[ACTIVE_KEY_INDEX] = currentIndex + 1
            }
            true
        } else {
            false // No more keys
        }
    }

    /**
     * Reset to first API key
     */
    suspend fun resetKeyIndex() {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_KEY_INDEX] = 0
        }
    }

    // ========== LEGACY SINGLE KEY (for backwards compatibility) ==========
    
    val apiKey: Flow<String> = currentApiKey

    suspend fun saveApiKey(key: String) {
        // Save as single key for legacy, also add to multi-key list
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
            val existingJson = preferences[API_KEYS_JSON] ?: ""
            val existingKeys = if (existingJson.isBlank()) emptyList() else existingJson.split(",")
            if (!existingKeys.contains(key)) {
                preferences[API_KEYS_JSON] = (existingKeys + key).joinToString(",")
            }
        }
    }

    // ========== MODEL NAME ==========
    
    val modelName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[MODEL_NAME] ?: "gemini-3-flash-preview"
        }

    suspend fun saveModelName(model: String) {
        context.dataStore.edit { preferences ->
            preferences[MODEL_NAME] = model
        }
    }

    // ========== READING STYLE ==========
    
    val readingStyle: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[READING_STYLE] ?: "Nghiêm túc"
        }
    
    suspend fun saveReadingStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[READING_STYLE] = style
        }
    }
}
