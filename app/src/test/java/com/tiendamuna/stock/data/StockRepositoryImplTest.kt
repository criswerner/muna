package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.StockDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteStockDataSource
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.service.IngredientIntegrityService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryImplTest {

    private lateinit var localDataSource: StockDataSource
    private lateinit var remoteDataSource: RemoteStockDataSource
    private lateinit var integrityService: IngredientIntegrityService
    private lateinit var repository: StockRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        remoteDataSource = mockk(relaxed = true)
        integrityService = mockk(relaxed = true)
        repository = StockRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            integrityService = integrityService,
            externalScope = testScope,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getStock should load from local and then remote when not loaded`() = runTest(testDispatcher) {
        // Given
        val localIngredients = listOf(mockk<Ingredient>(relaxed = true))
        val remoteIngredients = listOf(mockk<Ingredient>(relaxed = true), mockk(relaxed = true))
        coEvery { localDataSource.getStock() } returns localIngredients
        coEvery { remoteDataSource.getStock() } returns Result.success(remoteIngredients)

        val results = mutableListOf<List<Ingredient>>()
        val job = launch {
            repository.getStock().collect { results.add(it) }
        }

        // When
        advanceUntilIdle()

        // Then
        assertEquals(remoteIngredients, results.last())
        coVerify { localDataSource.saveIngredients(remoteIngredients) }
        job.cancel()
    }

    @Test
    fun `addIngredient should update local and sync to remote`() = runTest(testDispatcher) {
        // Given
        val ingredient = Ingredient(name = "Sugar", quantity = 1000.0, unit = "g")
        
        // When
        repository.addIngredient(ingredient)
        advanceUntilIdle()

        // Then
        coVerify { localDataSource.saveIngredients(match { it.contains(ingredient) }) }
        coVerify { remoteDataSource.addOrUpdateIngredient(ingredient) }
    }

    @Test
    fun `updateIngredient should update local and sync to remote and call integrity service if name changed`() = runTest(testDispatcher) {
        // Given
        val oldIngredient = Ingredient(id = "1", name = "Salt", quantity = 500.0, unit = "g")
        val newIngredient = Ingredient(id = "1", name = "Fine Salt", quantity = 500.0, unit = "g")
        
        coEvery { localDataSource.getStock() } returns listOf(oldIngredient)
        coEvery { remoteDataSource.getStock() } returns Result.success(listOf(oldIngredient))
        
        // Initial load
        val job = launch { repository.getStock().collect {} }
        advanceUntilIdle()

        // When
        repository.updateIngredient(newIngredient)
        advanceUntilIdle()

        // Then
        coVerify { remoteDataSource.addOrUpdateIngredient(newIngredient) }
        coVerify { integrityService.propagateIngredientNameChange(newIngredient) }
        job.cancel()
    }

    @Test
    fun `deleteIngredient should update local and sync to remote`() = runTest(testDispatcher) {
        // Given
        val ingredient = Ingredient(id = "1", name = "Water", quantity = 2.0, unit = "l")
        
        // When
        repository.deleteIngredient(ingredient)
        advanceUntilIdle()

        // Then
        coVerify { localDataSource.saveIngredients(match { !it.contains(ingredient) }) }
        coVerify { remoteDataSource.deleteIngredient(ingredient.id) }
    }
}
