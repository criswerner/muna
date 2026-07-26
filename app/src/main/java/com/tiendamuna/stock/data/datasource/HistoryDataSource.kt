package com.tiendamuna.stock.data.datasource

import com.tiendamuna.stock.domain.model.PreparationHistory

interface HistoryDataSource {
    suspend fun getHistory(): List<PreparationHistory>
    suspend fun saveHistory(history: List<PreparationHistory>)
}
