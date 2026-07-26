package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.StockRepository
import com.tiendamuna.stock.domain.util.UnitConverter
import kotlinx.coroutines.flow.first

class PrepareRecipeUseCase(private val stockRepository: StockRepository) {
    suspend operator fun invoke(recipe: Recipe, batches: Double = 1.0) {
        if (batches <= 0) throw IllegalArgumentException("La cantidad de lotes debe ser mayor a 0")
        
        val currentStock = stockRepository.getStock().first()
        
        // Check if all ingredients are available in sufficient quantity (with conversion and batch multiplier)
        val missingIngredients = recipe.ingredients.filter { required ->
            val stockItem = currentStock.find { it.id == required.ingredientId }
            if (stockItem == null) return@filter true
            
            val totalNeededForBatches = required.quantityRequired * batches
            
            val convertedRequiredQuantity = UnitConverter.convert(
                amount = totalNeededForBatches,
                fromUnitSymbol = required.unit,
                toUnitSymbol = stockItem.unit
            )
            
            stockItem.quantity < convertedRequiredQuantity
        }

        if (missingIngredients.isNotEmpty()) {
            val details = missingIngredients.joinToString(", ") { it.name }
            throw IllegalStateException("Stock insuficiente para preparar $batches lotes de '${recipe.name}'. Faltan: $details")
        }

        // Subtract from stock
        recipe.ingredients.forEach { required ->
            val stockItem = currentStock.find { it.id == required.ingredientId }!!
            
            val totalNeededForBatches = required.quantityRequired * batches

            val convertedRequiredQuantity = UnitConverter.convert(
                amount = totalNeededForBatches,
                fromUnitSymbol = required.unit,
                toUnitSymbol = stockItem.unit
            )
            
            stockRepository.updateIngredient(
                stockItem.copy(quantity = stockItem.quantity - convertedRequiredQuantity)
            )
        }
    }
}
