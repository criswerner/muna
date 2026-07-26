package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetHistoryUseCase(private val repository: HistoryRepository) {
    operator fun invoke(): Flow<List<PreparationHistory>> {
        return repository.getHistory().map { list ->
            list.sortedByDescending { it.timestamp }
        }
    }
}
