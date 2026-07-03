package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.RecipeRepository

class DeleteRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.deleteRecipe(recipe)
    }
}
