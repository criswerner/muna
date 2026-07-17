package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import com.tiendamuna.stock.domain.util.UnitConverter
import kotlinx.coroutines.flow.first

class AddIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(name: String, quantity: Double, unit: String, category: Category = Category.OTHERS) {
        if (name.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío")
        if (quantity < 0) throw IllegalArgumentException("La cantidad no puede ser negativa")
        
        val currentStock = repository.getStock().first()
        val existingIngredient = currentStock.find { it.name.trim().lowercase() == name.trim().lowercase() }
        
        if (existingIngredient != null) {
            // Si el ingrediente existe, intentamos combinar las cantidades
            if (UnitConverter.areCompatible(unit, existingIngredient.unit)) {
                val convertedQuantity = UnitConverter.convert(
                    amount = quantity,
                    fromUnitSymbol = unit,
                    toUnitSymbol = existingIngredient.unit
                )
                val updatedIngredient = existingIngredient.copy(
                    quantity = existingIngredient.quantity + convertedQuantity
                )
                repository.updateIngredient(updatedIngredient)
            } else {
                // Si las unidades no son compatibles (ej: kg vs litros), lanzamos error
                throw IllegalArgumentException("Ya existe '$name' con una unidad incompatible (${existingIngredient.unit}). No se puede combinar.")
            }
        } else {
            // Si no existe, lo agregamos normalmente
            val ingredient = Ingredient(name = name, quantity = quantity, unit = unit, category = category)
            repository.addIngredient(ingredient)
        }
    }
}
