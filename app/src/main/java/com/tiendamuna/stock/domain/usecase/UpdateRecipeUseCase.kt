package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.DomainException
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.RecipeRepository

class UpdateRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.name.isBlank()) throw DomainException.EmptyName
        if (recipe.ingredients.isEmpty()) throw DomainException.NoIngredientsInRecipe

        repository.updateRecipe(recipe)
    }
}
