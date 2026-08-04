package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.domain.repository.HistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class HistoryUseCasesTest {

    private lateinit var repository: HistoryRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `AddHistoryEntryUseCase should call repository addEntry`() = runTest {
        val useCase = AddHistoryEntryUseCase(repository)
        val entry = PreparationHistory(
            recipeId = "1",
            recipeName = "Test",
            batchesPrepared = 1.0,
            totalProducedQuantity = 10.0,
            yieldUnit = "u.",
            totalCost = 100.0
        )

        useCase(entry)

        coVerify { repository.addEntry(entry) }
    }

    @Test
    fun `GetHistoryUseCase should return history sorted by timestamp descending`() = runTest {
        val useCase = GetHistoryUseCase(repository)
        val entryOld = PreparationHistory(
            recipeId = "1", recipeName = "Old", batchesPrepared = 1.0,
            totalProducedQuantity = 1.0, yieldUnit = "u.", totalCost = 10.0,
            timestamp = Date(1000L)
        )
        val entryNew = PreparationHistory(
            recipeId = "2", recipeName = "New", batchesPrepared = 1.0,
            totalProducedQuantity = 1.0, yieldUnit = "u.", totalCost = 10.0,
            timestamp = Date(2000L)
        )
        
        coEvery { repository.getHistory() } returns flowOf(listOf(entryOld, entryNew))

        val result = mutableListOf<List<PreparationHistory>>()
        useCase().collect { result.add(it) }

        assertEquals(2, result[0].size)
        assertEquals("New", result[0][0].recipeName)
        assertEquals("Old", result[0][1].recipeName)
    }
}
