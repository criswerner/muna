package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository

class DeleteIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(ingredient: Ingredient) {
        repository.deleteIngredient(ingredient)
    }
}
