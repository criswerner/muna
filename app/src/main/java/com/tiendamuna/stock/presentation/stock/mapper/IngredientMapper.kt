package com.tiendamuna.stock.presentation.stock.mapper

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel

fun Ingredient.toUiModel(): IngredientUiModel {
    return IngredientUiModel(
        id = id,
        name = name,
        quantityDisplay = "$quantity $unit",
        rawQuantity = quantity,
        unit = unit
    )
}
