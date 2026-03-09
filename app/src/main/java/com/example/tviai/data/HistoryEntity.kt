package com.example.tviai.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "laso_history", indices = [Index(value = ["timestamp"])])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val birthDate: String, // Solar date for display
    val birthTime: String, // Hour string
    val gender: String,
    val cuc: String,
    val jsonData: String, // Serialized LasoData
    val timestamp: Long
)
