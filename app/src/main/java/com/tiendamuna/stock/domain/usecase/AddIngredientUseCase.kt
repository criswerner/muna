package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.DomainException
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import com.tiendamuna.stock.domain.util.PriceCalculator
import com.tiendamuna.stock.domain.util.UnitConverter
import kotlinx.coroutines.flow.first

class AddIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(
        name: String, 
        quantity: Double, 
        unit: String, 
        category: Category = Category.OTHERS,
        totalPrice: Double = 0.0,
        minThreshold: Double? = null
    ) {
        if (name.isBlank()) throw DomainException.EmptyName
        if (quantity <= 0) throw DomainException.InvalidQuantity
        
        val currentStock = repository.getStock().first()
        val existingIngredient = currentStock.find { it.name.trim().lowercase() == name.trim().lowercase() }
        
        if (existingIngredient != null) {
            if (UnitConverter.areCompatible(unit, existingIngredient.unit)) {
                val convertedAddedQuantity = UnitConverter.convert(
                    amount = quantity,
                    fromUnitSymbol = unit,
                    toUnitSymbol = existingIngredient.unit
                )
                
                val newWeightedUnitPrice = PriceCalculator.calculateWeightedAveragePrice(
                    currentQuantity = existingIngredient.quantity,
                    currentUnitPrice = existingIngredient.pricePerUnit,
                    addedQuantity = convertedAddedQuantity,
                    totalAddedPrice = totalPrice
                )
                
                val updatedIngredient = existingIngredient.copy(
                    quantity = existingIngredient.quantity + convertedAddedQuantity,
                    pricePerUnit = newWeightedUnitPrice,
                    minThreshold = minThreshold ?: existingIngredient.minThreshold
                )
                repository.updateIngredient(updatedIngredient)
            } else {
                throw DomainException.IncompatibleUnits(name, existingIngredient.unit)
            }
        } else {
            // New ingredient
            val unitPrice = if (quantity > 0) totalPrice / quantity else 0.0
            val ingredient = Ingredient(
                name = name, 
                quantity = quantity, 
                unit = unit, 
                category = category,
                pricePerUnit = unitPrice,
                minThreshold = minThreshold
            )
            repository.addIngredient(ingredient)
        }
    }
}
