package com.tiendamuna.stock.presentation.recipe.mapper

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.domain.util.UnitConverter
import com.tiendamuna.stock.presentation.recipe.model.RecipeIngredientUiModel
import com.tiendamuna.stock.presentation.recipe.model.RecipeUiModel
import java.util.Locale

fun Recipe.toUiModel(stock: List<Ingredient>): RecipeUiModel {
    val uiIngredients = ingredients.map { it.toUiModel(stock) }
    val totalCost = uiIngredients.sumOf { it.cost }
    val costPerUnit = if (yieldQuantity > 0) totalCost / yieldQuantity else 0.0
    
    return RecipeUiModel(
        id = id,
        name = name,
        ingredients = uiIngredients,
        instructions = instructions,
        totalCost = totalCost,
        costDisplay = "$${String.format(Locale.getDefault(), "%,.2f", totalCost)}",
        yieldDisplay = "${String.format(Locale.getDefault(), "%.2f", yieldQuantity)} $yieldUnit",
        yieldQuantity = yieldQuantity,
        yieldUnit = yieldUnit,
        costPerYieldUnitDisplay = "$${String.format(Locale.getDefault(), "%,.2f", costPerUnit)}/$yieldUnit"
    )
}

fun RecipeIngredient.toUiModel(stock: List<Ingredient>): RecipeIngredientUiModel {
    val stockItem = stock.find { it.id == ingredientId }
    val cost = if (stockItem != null) {
        val convertedQuantity = UnitConverter.convert(
            amount = quantityRequired,
            fromUnitSymbol = unit,
            toUnitSymbol = stockItem.unit
        )
        convertedQuantity * stockItem.pricePerUnit
    } else 0.0

    return RecipeIngredientUiModel(
        ingredientId = ingredientId,
        name = name,
        quantityDisplay = "${String.format(Locale.getDefault(), "%.2f", quantityRequired)} $unit",
        rawQuantity = quantityRequired,
        unit = unit,
        cost = cost,
        costDisplay = "$${String.format(Locale.getDefault(), "%,.2f", cost)}"
    )
}
