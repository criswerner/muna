package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.first

class UpdateIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(ingredient: Ingredient) {
        if (ingredient.name.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío")
        if (ingredient.quantity < 0) throw IllegalArgumentException("La cantidad no puede ser negativa")
        if (ingredient.pricePerUnit < 0) throw IllegalArgumentException("El precio no puede ser negativo")
        
        val currentStock = repository.getStock().first()
        val nameAlreadyTaken = currentStock.any { 
            it.id != ingredient.id && it.name.trim().lowercase() == ingredient.name.trim().lowercase() 
        }
        
        if (nameAlreadyTaken) {
            throw IllegalArgumentException("No se puede renombrar: ya existe otro ingrediente con el nombre '${ingredient.name}'")
        }

        repository.updateIngredient(ingredient)
    }
}
