package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository

class AddIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(name: String, quantity: Double, unit: String) {
        if (name.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío")
        if (quantity < 0) throw IllegalArgumentException("La cantidad no puede ser negativa")
        
        val ingredient = Ingredient(name = name, quantity = quantity, unit = unit)
        repository.addIngredient(ingredient)
    }
}
