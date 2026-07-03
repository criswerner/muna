package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetRecipesUseCase(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<List<Recipe>> {
        return repository.getRecipes()
    }
}
