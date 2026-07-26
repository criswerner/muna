package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.HistoryDataSource
import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryRepositoryImpl(
    private val dataSource: HistoryDataSource
) : HistoryRepository {

    private val _history = MutableStateFlow<List<PreparationHistory>>(emptyList())
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            _history.value = dataSource.getHistory()
        }
    }

    override fun getHistory(): Flow<List<PreparationHistory>> = _history.asStateFlow()

    override suspend fun addEntry(entry: PreparationHistory) {
        val updated = listOf(entry) + _history.value
        dataSource.saveHistory(updated)
        _history.value = updated
    }

    override suspend fun clearHistory() {
        dataSource.saveHistory(emptyList())
        _history.value = emptyList()
    }
}
