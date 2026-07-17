package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import com.tiendamuna.stock.domain.util.UnitConverter
import kotlinx.coroutines.flow.first

class AddIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(
        name: String, 
        quantity: Double, 
        unit: String, 
        category: Category = Category.OTHERS,
        totalPrice: Double = 0.0
    ) {
        if (name.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío")
        if (quantity <= 0) throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        
        val unitPriceForAddedBatch = totalPrice / quantity
        
        val currentStock = repository.getStock().first()
        val existingIngredient = currentStock.find { it.name.trim().lowercase() == name.trim().lowercase() }
        
        if (existingIngredient != null) {
            if (UnitConverter.areCompatible(unit, existingIngredient.unit)) {
                val convertedAddedQuantity = UnitConverter.convert(
                    amount = quantity,
                    fromUnitSymbol = unit,
                    toUnitSymbol = existingIngredient.unit
                )
                
                // Calculamos el nuevo precio promedio ponderado
                val totalValueExisting = existingIngredient.quantity * existingIngredient.pricePerUnit
                val totalValueAdded = totalPrice 
                val newTotalQuantity = existingIngredient.quantity + convertedAddedQuantity
                
                val newWeightedUnitPrice = (totalValueExisting + totalValueAdded) / newTotalQuantity
                
                val updatedIngredient = existingIngredient.copy(
                    quantity = newTotalQuantity,
                    pricePerUnit = newWeightedUnitPrice
                )
                repository.updateIngredient(updatedIngredient)
            } else {
                throw IllegalArgumentException("Ya existe '$name' con una unidad incompatible (${existingIngredient.unit}). No se puede combinar.")
            }
        } else {
            // New ingredient
            val ingredient = Ingredient(
                name = name, 
                quantity = quantity, 
                unit = unit, 
                category = category,
                pricePerUnit = unitPriceForAddedBatch
            )
            repository.addIngredient(ingredient)
        }
    }
}
