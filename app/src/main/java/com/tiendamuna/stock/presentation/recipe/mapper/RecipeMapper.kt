package com.tiendamuna.stock.presentation.recipe.mapper

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.domain.util.UnitConverter
import com.tiendamuna.stock.presentation.recipe.model.RecipeIngredientUiModel
import com.tiendamuna.stock.presentation.recipe.model.RecipeUiModel

fun Recipe.toUiModel(stock: List<Ingredient>): RecipeUiModel {
    val uiIngredients = ingredients.map { it.toUiModel(stock) }
    val totalCost = uiIngredients.sumOf { it.cost }
    return RecipeUiModel(
        id = id,
        name = name,
        ingredients = uiIngredients,
        instructions = instructions,
        totalCost = totalCost,
        costDisplay = "$${String.format("%.2f", totalCost)}"
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
        quantityDisplay = "$quantityRequired $unit",
        rawQuantity = quantityRequired,
        unit = unit,
        cost = cost,
        costDisplay = "$${String.format("%.2f", cost)}"
    )
}
