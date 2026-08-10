package com.tiendamuna.stock.data.service

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.RecipeRepository
import com.tiendamuna.stock.domain.service.IngredientIntegrityService
import kotlinx.coroutines.flow.first

class IngredientIntegrityServiceImpl(
    private val recipeRepository: RecipeRepository
) : IngredientIntegrityService {

    override suspend fun propagateIngredientNameChange(ingredient: Ingredient) {
        val allRecipes = recipeRepository.getRecipes().first()
        val recipesToUpdate = allRecipes.filter { recipe ->
            recipe.ingredients.any { it.ingredientId == ingredient.id }
        }
        
        recipesToUpdate.forEach { recipe ->
            val updatedIngredients = recipe.ingredients.map { recipeIng ->
                if (recipeIng.ingredientId == ingredient.id) {
                    recipeIng.copy(name = ingredient.name)
                } else {
                    recipeIng
                }
            }
            recipeRepository.updateRecipe(recipe.copy(ingredients = updatedIngredients))
        }
    }
}
