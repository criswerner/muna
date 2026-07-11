package com.tiendamuna.stock.presentation.stock.model

import androidx.compose.runtime.Immutable

@Immutable
data class IngredientUiModel(
    val id: String,
    val name: String,
    val quantityDisplay: String,
    val rawQuantity: Double,
    val unit: String,
    val categoryName: String
)
