package com.example.tviai.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Singleton logger for Supabase sync debugging.
 * Stores logs in-memory so they can be displayed in DebugLogScreen.
 */
object SyncLogger {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun log(message: String) {
        val timestamp = timeFormat.format(Date())
        val entry = "[$timestamp] $message"
        _logs.value = _logs.value + entry
        android.util.Log.d("SyncLogger", entry)
    }

    fun logError(message: String, throwable: Throwable? = null) {
        val timestamp = timeFormat.format(Date())
        val errorDetail = throwable?.let { ": ${it.javaClass.simpleName} - ${it.message}" } ?: ""
        val entry = "[$timestamp] ❌ ERROR: $message$errorDetail"
        _logs.value = _logs.value + entry
        android.util.Log.e("SyncLogger", entry, throwable)
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun getAllLogsAsText(): String {
        return _logs.value.joinToString("\n")
    }
}
