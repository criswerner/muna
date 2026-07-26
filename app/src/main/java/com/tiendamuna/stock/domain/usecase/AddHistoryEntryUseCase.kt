package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.domain.repository.HistoryRepository

class AddHistoryEntryUseCase(private val repository: HistoryRepository) {
    suspend operator fun invoke(entry: PreparationHistory) {
        repository.addEntry(entry)
    }
}
