package com.tiendamuna.stock.presentation.recipe.mapper

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.presentation.recipe.model.RecipeIngredientUiModel
import com.tiendamuna.stock.presentation.recipe.model.RecipeUiModel

fun Recipe.toUiModel(): RecipeUiModel {
    return RecipeUiModel(
        id = id,
        name = name,
        ingredients = ingredients.map { it.toUiModel() },
        instructions = instructions
    )
}

fun RecipeIngredient.toUiModel(): RecipeIngredientUiModel {
    return RecipeIngredientUiModel(
        ingredientId = ingredientId,
        name = name,
        quantityDisplay = "$quantityRequired $unit",
        rawQuantity = quantityRequired,
        unit = unit
    )
}
