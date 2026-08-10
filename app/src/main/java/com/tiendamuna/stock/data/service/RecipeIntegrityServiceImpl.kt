package com.tiendamuna.stock.data.service

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.HistoryRepository
import com.tiendamuna.stock.domain.service.RecipeIntegrityService
import kotlinx.coroutines.flow.first

class RecipeIntegrityServiceImpl(
    private val historyRepository: HistoryRepository
) : RecipeIntegrityService {

    override suspend fun propagateRecipeNameChange(recipe: Recipe) {
        val allHistory = historyRepository.getHistory().first()
        val entriesToUpdate = allHistory.filter { it.recipeId == recipe.id }
        
        entriesToUpdate.forEach { entry ->
            if (entry.recipeName != recipe.name) {
                historyRepository.addEntry(entry.copy(recipeName = recipe.name))
            }
        }
    }
}
