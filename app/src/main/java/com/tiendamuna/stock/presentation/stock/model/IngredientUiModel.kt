package com.tiendamuna.stock.presentation.stock.model

import androidx.compose.runtime.Immutable

enum class StockStatus {
    NORMAL,
    LOW_STOCK,
    OUT_OF_STOCK
}

@Immutable
data class IngredientUiModel(
    val id: String,
    val name: String,
    val quantityDisplay: String,
    val rawQuantity: Double,
    val unit: String,
    val categoryName: String,
    val pricePerUnit: Double,
    val priceDisplay: String,
    val minThreshold: Double?,
    val status: StockStatus = StockStatus.NORMAL
)
