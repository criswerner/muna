package com.tiendamuna.stock.presentation.stock

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.usecase.AddIngredientUseCase
import com.tiendamuna.stock.domain.usecase.DeleteIngredientUseCase
import com.tiendamuna.stock.domain.usecase.GetStockUseCase
import com.tiendamuna.stock.domain.usecase.UpdateIngredientUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var getStockUseCase: GetStockUseCase
    private lateinit var addIngredientUseCase: AddIngredientUseCase
    private lateinit var updateIngredientUseCase: UpdateIngredientUseCase
    private lateinit var deleteIngredientUseCase: DeleteIngredientUseCase
    private lateinit var viewModel: StockViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        getStockUseCase = mockk()
        addIngredientUseCase = mockk(relaxed = true)
        updateIngredientUseCase = mockk(relaxed = true)
        deleteIngredientUseCase = mockk(relaxed = true)
        
        // Default behavior for init block
        coEvery { getStockUseCase() } returns flowOf(emptyList())
        
        viewModel = StockViewModel(
            getStockUseCase,
            addIngredientUseCase,
            updateIngredientUseCase,
            deleteIngredientUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        val collectJob = launch { viewModel.state.collect {} }
        val state = viewModel.state.value
        assertEquals("", state.searchQuery)
        assertEquals(0, state.ingredients.size)
        assertNull(state.error)
        collectJob.cancel()
    }

    @Test
    fun `when stock is loaded should update state`() = runTest {
        // Given
        val ingredients = listOf(Ingredient(name = "Harina", quantity = 10.0, unit = "kg"))
        coEvery { getStockUseCase() } returns flowOf(ingredients)
        
        // Re-init to trigger the flow collection in init block
        viewModel = StockViewModel(getStockUseCase, addIngredientUseCase, updateIngredientUseCase, deleteIngredientUseCase)
        val collectJob = launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.ingredients.size)
        assertEquals("Harina", viewModel.state.value.ingredients[0].name)
        collectJob.cancel()
    }

    @Test
    fun `when search query changes should filter ingredients`() = runTest {
        // Given
        val ingredients = listOf(
            Ingredient(name = "Harina", quantity = 10.0, unit = "kg"),
            Ingredient(name = "Sal", quantity = 1.0, unit = "kg")
        )
        coEvery { getStockUseCase() } returns flowOf(ingredients)
        viewModel = StockViewModel(getStockUseCase, addIngredientUseCase, updateIngredientUseCase, deleteIngredientUseCase)
        val collectJob = launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // When
        viewModel.onEvent(StockEvent.SearchQueryChanged("Sal"))
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.ingredients.size)
        assertEquals("Sal", viewModel.state.value.ingredients[0].name)
        collectJob.cancel()
    }

    @Test
    fun `when add ingredient fails should update error state`() = runTest {
        // Given
        val errorMessage = "Error de validación"
        coEvery { 
            addIngredientUseCase(any(), any(), any(), any(), any()) 
        } throws IllegalArgumentException(errorMessage)

        val collectJob = launch { viewModel.state.collect {} }

        // When
        viewModel.onEvent(StockEvent.AddIngredient("Harina", 10.0, "kg", mockk(), 100.0))
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        collectJob.cancel()
    }
}
