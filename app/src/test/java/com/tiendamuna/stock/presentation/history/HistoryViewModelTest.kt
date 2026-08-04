package com.tiendamuna.stock.presentation.history

import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.domain.usecase.GetHistoryUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getHistoryUseCase: GetHistoryUseCase
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getHistoryUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when history is loaded should update state with UI models`() = runTest {
        // Given
        val history = listOf(
            PreparationHistory(
                recipeName = "Pan",
                batchesPrepared = 1.0,
                totalProducedQuantity = 1.0,
                yieldUnit = "u.",
                totalCost = 10.0,
                timestamp = Date()
            )
        )
        coEvery { getHistoryUseCase() } returns flowOf(history)

        // When
        viewModel = HistoryViewModel(getHistoryUseCase)
        
        // Empezar a colectar el StateFlow para que se active WhileSubscribed
        val collectJob = launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.entries.size)
        assertEquals("Pan", state.entries[0].recipeName)
        assertEquals(false, state.isLoading)
        
        collectJob.cancel()
    }
}
