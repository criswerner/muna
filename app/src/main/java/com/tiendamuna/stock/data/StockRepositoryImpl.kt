package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.StockDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteStockDataSource
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockRepositoryImpl(
    private val localDataSource: StockDataSource,
    private val remoteDataSource: RemoteStockDataSource
) : StockRepository {
    
    // The Repository is now the Single Source of Truth in memory
    private val _stock = MutableStateFlow<List<Ingredient>>(emptyList())
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Initial load
        scope.launch {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        // 1. Intentar obtener de remoto
        remoteDataSource.getStock()
            .onSuccess { remoteIngredients ->
                // 2. Si tiene éxito, actualizar local y memoria
                localDataSource.saveIngredients(remoteIngredients)
                _stock.value = remoteIngredients
            }
            .onFailure {
                // 3. Si falla, usar local como fallback
                _stock.value = localDataSource.getStock()
            }
    }

    override fun getStock(): Flow<List<Ingredient>> = _stock.asStateFlow()

    override suspend fun addIngredient(ingredient: Ingredient) {
        val updated = _stock.value + ingredient
        saveAndUpdate(updated)
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        val updated = _stock.value.map { if (it.id == ingredient.id) ingredient else it }
        saveAndUpdate(updated)
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        val updated = _stock.value.filter { it.id != ingredient.id }
        saveAndUpdate(updated)
    }

    private suspend fun saveAndUpdate(newList: List<Ingredient>) {
        // Actualizar local y memoria inmediatamente (Optimistic UI)
        localDataSource.saveIngredients(newList)
        _stock.value = newList
        
        // Intentar sincronizar con remoto en segundo plano
        scope.launch {
            remoteDataSource.syncStock(newList)
        }
    }
}
