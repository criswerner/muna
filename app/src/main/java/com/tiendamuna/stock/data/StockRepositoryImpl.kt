package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.StockDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteStockDataSource
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import com.tiendamuna.stock.domain.service.IngredientIntegrityService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StockRepositoryImpl(
    private val localDataSource: StockDataSource,
    private val remoteDataSource: RemoteStockDataSource,
    private val integrityService: IngredientIntegrityService,
    private val externalScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StockRepository {
    
    private val _stock = MutableStateFlow<List<Ingredient>>(emptyList())
    private var isLoaded = false

    override fun getStock(): Flow<List<Ingredient>> = _stock.asStateFlow()
        .onStart {
            if (!isLoaded) {
                refreshStock()
            }
        }

    private fun refreshStock() {
        externalScope.launch(ioDispatcher) {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        val localData = localDataSource.getStock()
        _stock.value = localData
        
        remoteDataSource.getStock()
            .onSuccess { remoteIngredients ->
                localDataSource.saveIngredients(remoteIngredients)
                _stock.value = remoteIngredients
                isLoaded = true
            }
            .onFailure {
                isLoaded = true 
            }
    }

    override suspend fun addIngredient(ingredient: Ingredient) {
        val updated = _stock.value + ingredient
        saveAndSync(updated) {
            remoteDataSource.addOrUpdateIngredient(ingredient)
        }
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        val oldIngredient = _stock.value.find { it.id == ingredient.id }
        val updated = _stock.value.map { if (it.id == ingredient.id) ingredient else it }
        
        // 1. Actualizar el stock (Local + Memoria + Firestore)
        saveAndSync(updated) {
            remoteDataSource.addOrUpdateIngredient(ingredient)
        }

        // 2. Si el nombre cambió, propagar a través del servicio de integridad
        if (oldIngredient != null && oldIngredient.name != ingredient.name) {
            integrityService.propagateIngredientNameChange(ingredient)
        }
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        val updated = _stock.value.filter { it.id != ingredient.id }
        saveAndSync(updated) {
            remoteDataSource.deleteIngredient(ingredient.id)
        }
    }

    private suspend fun saveAndSync(newList: List<Ingredient>, remoteAction: suspend () -> Result<Unit>) {
        withContext(ioDispatcher) {
            localDataSource.saveIngredients(newList)
        }
        _stock.value = newList
        
        externalScope.launch(ioDispatcher) {
            remoteAction()
        }
    }
}
