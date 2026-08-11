package com.tiendamuna.stock.presentation.stock.mapper

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import com.tiendamuna.stock.presentation.stock.model.StockStatus
import java.util.Locale

fun Ingredient.toUiModel(): IngredientUiModel {
    val status = when {
        quantity <= 0 -> StockStatus.OUT_OF_STOCK
        minThreshold != null && quantity < minThreshold -> StockStatus.LOW_STOCK
        else -> StockStatus.NORMAL
    }

    val valuation = quantity * pricePerUnit

    return IngredientUiModel(
        id = id,
        name = name,
        quantityDisplay = "${String.format(Locale.getDefault(), "%.2f", quantity)} $unit",
        rawQuantity = quantity,
        unit = unit,
        categoryName = category.displayName,
        pricePerUnit = pricePerUnit,
        priceDisplay = "$${String.format(Locale.getDefault(), "%.2f", pricePerUnit)}/$unit",
        minThreshold = minThreshold,
        status = status,
        valuation = valuation,
        valuationDisplay = "$${String.format(Locale.getDefault(), "%,.2f", valuation)}"
    )
}
