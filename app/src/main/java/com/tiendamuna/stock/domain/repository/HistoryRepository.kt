package com.tiendamuna.stock.domain.repository

import com.tiendamuna.stock.domain.model.PreparationHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(): Flow<List<PreparationHistory>>
    suspend fun addEntry(entry: PreparationHistory)
    suspend fun clearHistory()
}
