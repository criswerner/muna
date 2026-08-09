package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.RecipeDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteRecipeDataSource
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeRepositoryImpl(
    private val localDataSource: RecipeDataSource,
    private val remoteDataSource: RemoteRecipeDataSource,
    private val externalScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecipeRepository {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private var isLoaded = false

    override fun getRecipes(): Flow<List<Recipe>> = _recipes.asStateFlow()
        .onStart {
            if (!isLoaded) {
                refreshRecipes()
            }
        }

    private fun refreshRecipes() {
        externalScope.launch(ioDispatcher) {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        // Cargar local primero
        val localData = localDataSource.getRecipes()
        _recipes.value = localData
        
        // Intentar obtener de remoto para actualizar
        remoteDataSource.getRecipes()
            .onSuccess { remoteRecipes ->
                localDataSource.saveRecipes(remoteRecipes)
                _recipes.value = remoteRecipes
                isLoaded = true
            }
            .onFailure {
                isLoaded = true
            }
    }

    override suspend fun addRecipe(recipe: Recipe) {
        val updated = _recipes.value + recipe
        saveAndSync(updated) {
            remoteDataSource.addOrUpdateRecipe(recipe)
        }
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val updated = _recipes.value.map { if (it.id == recipe.id) recipe else it }
        saveAndSync(updated) {
            remoteDataSource.addOrUpdateRecipe(recipe)
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        val updated = _recipes.value.filter { it.id != recipe.id }
        saveAndSync(updated) {
            remoteDataSource.deleteRecipe(recipe.id)
        }
    }

    private suspend fun saveAndSync(newList: List<Recipe>, remoteAction: suspend () -> Result<Unit>) {
        // Optimistic UI
        withContext(ioDispatcher) {
            localDataSource.saveRecipes(newList)
        }
        _recipes.value = newList
        
        // Sincronización remota en segundo plano
        externalScope.launch(ioDispatcher) {
            remoteAction()
        }
    }
}
