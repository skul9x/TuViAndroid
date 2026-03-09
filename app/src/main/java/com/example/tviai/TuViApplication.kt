package com.example.tviai

import android.app.Application
import androidx.room.Room
import com.example.tviai.data.HistoryDatabase
import com.example.tviai.data.HistoryRepository
import com.example.tviai.data.SettingsDataStore

class TuViApplication : Application() {
    
    // Manual Dependency Injection container
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(private val context: android.content.Context) {
    private val database by lazy {
        Room.databaseBuilder(
            context,
            HistoryDatabase::class.java,
            "laso_history.db"
        ).build()
    }

    val historyRepository by lazy {
        HistoryRepository(database.historyDao())
    }
    
    val settingsDataStore by lazy {
        SettingsDataStore(context)
    }
}
