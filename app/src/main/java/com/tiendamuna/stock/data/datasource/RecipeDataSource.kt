package com.tiendamuna.stock.data.datasource

import com.tiendamuna.stock.domain.model.Recipe

interface RecipeDataSource {
    suspend fun getRecipes(): List<Recipe>
    suspend fun saveRecipes(recipes: List<Recipe>)
}
