package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow

class GetStockUseCase(private val repository: StockRepository) {
    operator fun invoke(): Flow<List<Ingredient>> {
        return repository.getStock()
    }
}
