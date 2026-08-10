package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.HistoryDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteHistoryDataSource
import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryRepositoryImpl(
    private val localDataSource: HistoryDataSource,
    private val remoteDataSource: RemoteHistoryDataSource,
    private val externalScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HistoryRepository {

    private val _history = MutableStateFlow<List<PreparationHistory>>(emptyList())
    private var isLoaded = false

    override fun getHistory(): Flow<List<PreparationHistory>> = _history.asStateFlow()
        .onStart {
            if (!isLoaded) {
                refreshHistory()
            }
        }

    private fun refreshHistory() {
        externalScope.launch(ioDispatcher) {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        // Cargar local primero
        val localData = localDataSource.getHistory()
        _history.value = localData
        
        // Intentar obtener de remoto para actualizar
        remoteDataSource.getHistory()
            .onSuccess { remoteHistory ->
                localDataSource.saveHistory(remoteHistory)
                _history.value = remoteHistory
                isLoaded = true
            }
            .onFailure {
                isLoaded = true
            }
    }

    override suspend fun addEntry(entry: PreparationHistory) {
        val updated = listOf(entry) + _history.value
        
        // Optimistic UI: Actualizar local y memoria
        withContext(ioDispatcher) {
            localDataSource.saveHistory(updated)
        }
        _history.value = updated
        
        // Sincronización remota en segundo plano
        externalScope.launch(ioDispatcher) {
            remoteDataSource.addHistoryEntry(entry)
        }
    }

    override suspend fun clearHistory() {
        // Para simplificar, solo limpiamos local y memoria en este MVP
        // Firestore requeriría borrar todos los documentos uno por uno o una Cloud Function
        withContext(ioDispatcher) {
            localDataSource.saveHistory(emptyList())
        }
        _history.value = emptyList()
    }
}
