package com.tiendamuna.stock.data.remote

import com.tiendamuna.stock.domain.model.Ingredient
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface StockApiService {
    @GET("v1/stock")
    suspend fun getStock(): Response<List<Ingredient>>

    @POST("v1/stock/sync")
    suspend fun syncStock(@Body ingredients: List<Ingredient>): Response<Unit>
}
