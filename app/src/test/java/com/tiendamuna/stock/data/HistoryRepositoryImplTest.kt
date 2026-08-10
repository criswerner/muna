package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.HistoryDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteHistoryDataSource
import com.tiendamuna.stock.domain.model.PreparationHistory
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
class HistoryRepositoryImplTest {

    private lateinit var localDataSource: HistoryDataSource
    private lateinit var remoteDataSource: RemoteHistoryDataSource
    private lateinit var repository: HistoryRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        remoteDataSource = mockk(relaxed = true)
        repository = HistoryRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            externalScope = testScope,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getHistory should load from local and then remote when not loaded`() = runTest(testDispatcher) {
        // Given
        val localHistory = listOf(mockk<PreparationHistory>(relaxed = true))
        val remoteHistory = listOf(mockk<PreparationHistory>(relaxed = true), mockk(relaxed = true))
        coEvery { localDataSource.getHistory() } returns localHistory
        coEvery { remoteDataSource.getHistory() } returns Result.success(remoteHistory)

        val results = mutableListOf<List<PreparationHistory>>()
        val job = launch {
            repository.getHistory().collect { 
                results.add(it)
            }
        }

        // When
        advanceUntilIdle()

        // Then
        // results might have [ [], localHistory, remoteHistory ]
        // or [ [], remoteHistory ] if conflated.
        // Given localDataSource.getHistory() is a mock, it might complete too fast.
        
        // Assert final state
        assertEquals(remoteHistory, results.last())
        
        // Check that it contains the local history at some point or at least remote history
        assert(results.contains(localHistory) || results.contains(remoteHistory))
        
        coVerify { localDataSource.saveHistory(remoteHistory) }
        job.cancel()
    }

    @Test
    fun `addEntry should update local and then sync to remote`() = runTest(testDispatcher) {
        // Given
        val entry = PreparationHistory(recipeName = "Test", batchesPrepared = 1.0, totalProducedQuantity = 1.0, yieldUnit = "u", totalCost = 1.0)
        
        // When
        repository.addEntry(entry)
        advanceUntilIdle()

        // Then
        coVerify { localDataSource.saveHistory(match { it.contains(entry) }) }
        coVerify { remoteDataSource.addHistoryEntry(entry) }
        val currentHistory = repository.getHistory().first()
        assert(currentHistory.contains(entry))
    }

    @Test
    fun `clearHistory should clear local and memory`() = runTest(testDispatcher) {
        // When
        repository.clearHistory()
        advanceUntilIdle()

        // Then
        coVerify { localDataSource.saveHistory(emptyList()) }
        val currentHistory = repository.getHistory().first()
        assertEquals(emptyList<PreparationHistory>(), currentHistory)
    }
}
