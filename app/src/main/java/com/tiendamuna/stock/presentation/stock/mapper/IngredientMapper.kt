package com.tiendamuna.stock.presentation.stock.mapper

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import com.tiendamuna.stock.presentation.stock.model.StockStatus

fun Ingredient.toUiModel(): IngredientUiModel {
    val status = when {
        quantity <= 0 -> StockStatus.OUT_OF_STOCK
        minThreshold != null && quantity < minThreshold -> StockStatus.LOW_STOCK
        else -> StockStatus.NORMAL
    }

    return IngredientUiModel(
        id = id,
        name = name,
        quantityDisplay = "$quantity $unit",
        rawQuantity = quantity,
        unit = unit,
        categoryName = category.displayName,
        pricePerUnit = pricePerUnit,
        priceDisplay = "$${String.format("%.2f", pricePerUnit)}/$unit",
        minThreshold = minThreshold,
        status = status
    )
}
