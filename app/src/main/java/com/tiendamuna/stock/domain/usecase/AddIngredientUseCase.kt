package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.first

class AddIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(name: String, quantity: Double, unit: String, category: Category = Category.OTHERS) {
        if (name.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío")
        if (quantity < 0) throw IllegalArgumentException("La cantidad no puede ser negativa")
        
        val currentStock = repository.getStock().first()
        val alreadyExists = currentStock.any { it.name.trim().lowercase() == name.trim().lowercase() }
        
        if (alreadyExists) {
            throw IllegalArgumentException("Ya existe un ingrediente con el nombre '$name'")
        }
        
        val ingredient = Ingredient(name = name, quantity = quantity, unit = unit, category = category)
        repository.addIngredient(ingredient)
    }
}
