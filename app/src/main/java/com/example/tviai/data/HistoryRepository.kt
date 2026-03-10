package com.example.tviai.data

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val historyDao: HistoryDao
) {
    private val gson = Gson()

    fun getAllHistory(): Flow<List<HistoryEntity>> {
        return historyDao.getAllHistory()
    }

    suspend fun saveLaso(data: LasoData) {
        val json = gson.toJson(data)
        val entity = HistoryEntity(
            name = data.info.name,
            birthDate = data.info.solarDate,
            birthTime = data.info.time,
            gender = data.info.gender,
            cuc = data.info.cuc,
            jsonData = json,
            timestamp = System.currentTimeMillis()
        )
        historyDao.insertHistory(entity)
    }
    
    suspend fun getLasoDetail(id: Long): LasoData? {
        val entity = historyDao.getHistoryById(id) ?: return null
        return try {
            gson.fromJson(entity.jsonData, LasoData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteHistory(entity: HistoryEntity) {
        historyDao.deleteHistory(entity)
    }
}
