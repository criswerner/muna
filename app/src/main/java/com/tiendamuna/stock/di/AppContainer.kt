package com.tiendamuna.stock.di

import android.content.Context
import com.tiendamuna.stock.data.HistoryRepositoryImpl
import com.tiendamuna.stock.data.RecipeRepositoryImpl
import com.tiendamuna.stock.data.StockRepositoryImpl
import com.tiendamuna.stock.data.datasource.local.SharedPrefsHistoryDataSource
import com.tiendamuna.stock.data.datasource.local.SharedPrefsRecipeDataSource
import com.tiendamuna.stock.data.datasource.local.SharedPrefsStockDataSource
import com.tiendamuna.stock.domain.usecase.*

/**
 * Dependency Container for Manual DI.
 * This manages the lifecycle of shared instances and builds the dependency tree.
 */
class AppContainer(context: Context) {

    // Data Sources
    private val stockDataSource = SharedPrefsStockDataSource(context)
    private val recipeDataSource = SharedPrefsRecipeDataSource(context)
    private val historyDataSource = SharedPrefsHistoryDataSource(context)

    // Repositories
    val stockRepository = StockRepositoryImpl(stockDataSource)
    val recipeRepository = RecipeRepositoryImpl(recipeDataSource)
    val historyRepository = HistoryRepositoryImpl(historyDataSource)

    // Use Cases (Stock)
    val getStockUseCase = GetStockUseCase(stockRepository)
    val addIngredientUseCase = AddIngredientUseCase(stockRepository)
    val updateIngredientUseCase = UpdateIngredientUseCase(stockRepository)
    val deleteIngredientUseCase = DeleteIngredientUseCase(stockRepository)

    // Use Cases (Recipe)
    val getRecipesUseCase = GetRecipesUseCase(recipeRepository)
    val addRecipeUseCase = AddRecipeUseCase(recipeRepository)
    val updateRecipeUseCase = UpdateRecipeUseCase(recipeRepository)
    val deleteRecipeUseCase = DeleteRecipeUseCase(recipeRepository)
    val prepareRecipeUseCase = PrepareRecipeUseCase(stockRepository)

    // Use Cases (History)
    val getHistoryUseCase = GetHistoryUseCase(historyRepository)
    val addHistoryEntryUseCase = AddHistoryEntryUseCase(historyRepository)
}
