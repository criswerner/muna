package com.tiendamuna.stock.data.datasource.remote

import com.tiendamuna.stock.data.remote.StockApiService
import com.tiendamuna.stock.domain.model.Ingredient

class RemoteStockDataSource(private val apiService: StockApiService) {

    suspend fun getStock(): Result<List<Ingredient>> {
        return try {
            val response = apiService.getStock()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error fetching stock: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncStock(ingredients: List<Ingredient>): Result<Unit> {
        return try {
            val response = apiService.syncStock(ingredients)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error syncing stock: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
