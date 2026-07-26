package com.tiendamuna.stock.presentation.recipe.model

import androidx.compose.runtime.Immutable

@Immutable
data class RecipeUiModel(
    val id: String,
    val name: String,
    val ingredients: List<RecipeIngredientUiModel>,
    val instructions: String,
    val totalCost: Double,
    val costDisplay: String,
    val yieldDisplay: String,
    val yieldQuantity: Double,
    val yieldUnit: String,
    val costPerYieldUnitDisplay: String
)

@Immutable
data class RecipeIngredientUiModel(
    val ingredientId: String,
    val name: String,
    val quantityDisplay: String,
    val rawQuantity: Double,
    val unit: String,
    val cost: Double,
    val costDisplay: String
)
