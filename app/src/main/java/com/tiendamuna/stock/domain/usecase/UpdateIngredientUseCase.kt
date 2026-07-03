package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository

class UpdateIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(ingredient: Ingredient) {
        if (ingredient.name.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío")
        if (ingredient.quantity < 0) throw IllegalArgumentException("La cantidad no puede ser negativa")
        repository.updateIngredient(ingredient)
    }
}
