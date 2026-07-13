package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.RecipeDataSource
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeRepositoryImpl(
    private val localDataSource: RecipeDataSource
) : RecipeRepository {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            _recipes.value = localDataSource.getRecipes()
        }
    }

    override fun getRecipes(): Flow<List<Recipe>> = _recipes.asStateFlow()

    override suspend fun addRecipe(recipe: Recipe) {
        val updated = _recipes.value + recipe
        saveAndUpdate(updated)
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val updated = _recipes.value.map { if (it.id == recipe.id) recipe else it }
        saveAndUpdate(updated)
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        val updated = _recipes.value.filter { it.id != recipe.id }
        saveAndUpdate(updated)
    }

    private suspend fun saveAndUpdate(newList: List<Recipe>) {
        localDataSource.saveRecipes(newList)
        _recipes.value = newList
    }
}
