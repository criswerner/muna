package com.tiendamuna.stock.domain.model

data class Recipe(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val ingredients: List<RecipeIngredient>,
    val instructions: String = "",
    val yieldQuantity: Double = 1.0,
    val yieldUnit: String = "u."
)

data class RecipeIngredient(
    val ingredientId: String,
    val name: String, // Cached name for display
    val quantityRequired: Double,
    val unit: String
)
