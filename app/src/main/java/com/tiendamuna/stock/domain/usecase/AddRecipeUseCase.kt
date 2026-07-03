package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.RecipeRepository

class AddRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.name.isBlank()) throw IllegalArgumentException("El nombre de la receta no puede estar vacío")
        if (recipe.ingredients.isEmpty()) throw IllegalArgumentException("La receta debe tener al menos un ingrediente")
        repository.addRecipe(recipe)
    }
}
