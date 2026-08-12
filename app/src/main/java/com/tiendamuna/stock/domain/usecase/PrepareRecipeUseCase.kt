package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.DomainException
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.StockRepository
import com.tiendamuna.stock.domain.util.UnitConverter
import kotlinx.coroutines.flow.first

class PrepareRecipeUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(recipe: Recipe, batches: Double) {
        if (batches <= 0) throw DomainException.InvalidQuantity
        
        val currentStock = repository.getStock().first()
        
        // 1. Validar si hay stock suficiente para todos los ingredientes
        recipe.ingredients.forEach { recipeIng ->
            val stockItem = currentStock.find { it.id == recipeIng.ingredientId }
            if (stockItem == null) throw DomainException.StockInsufficient
            
            val neededAmount = UnitConverter.convert(
                amount = recipeIng.quantityRequired * batches,
                fromUnitSymbol = recipeIng.unit,
                toUnitSymbol = stockItem.unit
            )
            
            if (stockItem.quantity < neededAmount) {
                throw DomainException.StockInsufficient
            }
        }

        // 2. Restar del stock
        recipe.ingredients.forEach { recipeIng ->
            val stockItem = currentStock.find { it.id == recipeIng.ingredientId }!!
            val amountToSubtract = UnitConverter.convert(
                amount = recipeIng.quantityRequired * batches,
                fromUnitSymbol = recipeIng.unit,
                toUnitSymbol = stockItem.unit
            )
            repository.updateIngredient(stockItem.copy(quantity = stockItem.quantity - amountToSubtract))
        }
    }
}
