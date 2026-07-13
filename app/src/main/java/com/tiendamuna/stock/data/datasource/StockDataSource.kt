package com.tiendamuna.stock.data.datasource

import com.tiendamuna.stock.domain.model.Ingredient

interface StockDataSource {
    suspend fun getStock(): List<Ingredient>
    suspend fun saveIngredients(ingredients: List<Ingredient>)
}
