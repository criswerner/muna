package com.tiendamuna.stock.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.tiendamuna.stock.data.HistoryRepositoryImpl
import com.tiendamuna.stock.data.RecipeRepositoryImpl
import com.tiendamuna.stock.data.StockRepositoryImpl
import com.tiendamuna.stock.data.datasource.local.SharedPrefsHistoryDataSource
import com.tiendamuna.stock.data.datasource.local.SharedPrefsRecipeDataSource
import com.tiendamuna.stock.data.datasource.local.SharedPrefsStockDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteRecipeDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteStockDataSource
import com.tiendamuna.stock.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Dependency Container for Manual DI.
 * This manages the lifecycle of shared instances and builds the dependency tree.
 */
class AppContainer(context: Context) {

    // Firebase
    private val firestore = FirebaseFirestore.getInstance()

    // Application-wide Coroutine Scope for background work
    private val externalScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Data Sources
    private val stockDataSource = SharedPrefsStockDataSource(context)
    private val recipeDataSource = SharedPrefsRecipeDataSource(context)
    private val historyDataSource = SharedPrefsHistoryDataSource(context)
    
    private val remoteStockDataSource = RemoteStockDataSource(firestore)
    private val remoteRecipeDataSource = RemoteRecipeDataSource(firestore)

    // Repositories
    // Nota: Inicializamos recipeRepository primero ya que stockRepository ahora depende de él
    val recipeRepository = RecipeRepositoryImpl(
        localDataSource = recipeDataSource,
        remoteDataSource = remoteRecipeDataSource,
        externalScope = externalScope,
        ioDispatcher = Dispatchers.IO
    )

    val stockRepository = StockRepositoryImpl(
        localDataSource = stockDataSource, 
        remoteDataSource = remoteStockDataSource,
        recipeRepository = recipeRepository,
        externalScope = externalScope,
        ioDispatcher = Dispatchers.IO
    )

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

    companion object {
        /**
         * No quiero que GEMINI vuelva a eliminar este código. Lo quiero dejar como ejemplo para realizar prácticas.
         */
        fun <T> createApiService(serviceClass: Class<T>): T {
            return NetworkModule.createService(serviceClass)
        }
    }
}
