package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.StockRepository
import com.tiendamuna.stock.domain.util.UnitConverter
import kotlinx.coroutines.flow.first

class PrepareRecipeUseCase(private val stockRepository: StockRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        val currentStock = stockRepository.getStock().first()
        
        // Check if all ingredients are available in sufficient quantity (with conversion)
        val missingIngredients = recipe.ingredients.filter { required ->
            val stockItem = currentStock.find { it.id == required.ingredientId }
            if (stockItem == null) return@filter true
            
            val convertedRequiredQuantity = UnitConverter.convert(
                amount = required.quantityRequired,
                fromUnit = required.unit,
                toUnit = stockItem.unit
            )
            
            stockItem.quantity < convertedRequiredQuantity
        }

        if (missingIngredients.isNotEmpty()) {
            val details = missingIngredients.joinToString(", ") { it.name }
            throw IllegalStateException("Stock insuficiente para: $details")
        }

        // Subtract from stock (with conversion)
        recipe.ingredients.forEach { required ->
            val stockItem = currentStock.find { it.id == required.ingredientId }!!
            
            val convertedRequiredQuantity = UnitConverter.convert(
                amount = required.quantityRequired,
                fromUnit = required.unit,
                toUnit = stockItem.unit
            )
            
            stockRepository.updateIngredient(
                stockItem.copy(quantity = stockItem.quantity - convertedRequiredQuantity)
            )
        }
    }
}
