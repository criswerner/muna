package com.tiendamuna.stock.domain.repository

import com.tiendamuna.stock.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStock(): Flow<List<Ingredient>>
    suspend fun addIngredient(ingredient: Ingredient)
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)
}
