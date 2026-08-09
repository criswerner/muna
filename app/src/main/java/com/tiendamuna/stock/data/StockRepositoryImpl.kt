package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.StockDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteStockDataSource
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class StockRepositoryImpl(
    private val localDataSource: StockDataSource,
    private val remoteDataSource: RemoteStockDataSource
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
        // Usamos GlobalScope solo para asegurar que la sincronización termine si el ViewModel desaparece,
        // pero la carga inicial es disparada por la UI
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        // 1. Cargar local inmediatamente para mostrar algo rápido
        _stock.value = localDataSource.getStock()
        
        // 2. Intentar obtener de remoto para actualizar
        remoteDataSource.getStock()
            .onSuccess { remoteIngredients ->
                localDataSource.saveIngredients(remoteIngredients)
                _stock.value = remoteIngredients
                isLoaded = true
            }
            .onFailure {
                // Si falla el remoto, ya tenemos lo local en el StateFlow
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
        val updated = _stock.value.map { if (it.id == ingredient.id) ingredient else it }
        saveAndSync(updated) {
            remoteDataSource.addOrUpdateIngredient(ingredient)
        }
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        val updated = _stock.value.filter { it.id != ingredient.id }
        saveAndSync(updated) {
            remoteDataSource.deleteIngredient(ingredient.id)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun saveAndSync(newList: List<Ingredient>, remoteAction: suspend () -> Result<Unit>) {
        // Optimistic UI: Actualizar local y memoria inmediatamente
        localDataSource.saveIngredients(newList)
        _stock.value = newList
        
        // La acción remota se lanza en GlobalScope para asegurar que persista 
        // aunque el usuario cierre la pantalla antes de que termine el network call
        GlobalScope.launch {
            remoteAction()
        }
    }
}
