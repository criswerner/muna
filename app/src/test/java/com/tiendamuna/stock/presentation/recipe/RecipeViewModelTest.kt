package com.tiendamuna.stock.presentation.recipe

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.usecase.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getRecipesUseCase: GetRecipesUseCase
    private lateinit var addRecipeUseCase: AddRecipeUseCase
    private lateinit var prepareRecipeUseCase: PrepareRecipeUseCase
    private lateinit var getStockUseCase: GetStockUseCase
    private lateinit var updateRecipeUseCase: UpdateRecipeUseCase
    private lateinit var deleteRecipeUseCase: DeleteRecipeUseCase
    private lateinit var addHistoryEntryUseCase: AddHistoryEntryUseCase
    private lateinit var viewModel: RecipeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getRecipesUseCase = mockk(relaxed = true)
        addRecipeUseCase = mockk(relaxed = true)
        prepareRecipeUseCase = mockk(relaxed = true)
        getStockUseCase = mockk(relaxed = true)
        updateRecipeUseCase = mockk(relaxed = true)
        deleteRecipeUseCase = mockk(relaxed = true)
        addHistoryEntryUseCase = mockk(relaxed = true)

        viewModel = RecipeViewModel(
            getRecipesUseCase,
            addRecipeUseCase,
            prepareRecipeUseCase,
            getStockUseCase,
            updateRecipeUseCase,
            deleteRecipeUseCase,
            addHistoryEntryUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty lists`() = runTest {
        val collectJob = launch { viewModel.state.collect {} }
        val state = viewModel.state.value
        assertEquals(0, state.recipes.size)
        assertEquals(0, state.availableIngredients.size)
        assertNull(state.error)
        collectJob.cancel()
    }

    @Test
    fun `when recipes are loaded should update state`() = runTest {
        // Given
        val recipes = listOf(Recipe(name = "Pan", ingredients = emptyList()))
        coEvery { getRecipesUseCase() } returns flowOf(recipes)
        
        val collectJob = launch { viewModel.state.collect {} }

        // When
        viewModel.loadRecipes()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.recipes.size)
        assertEquals("Pan", viewModel.state.value.recipes[0].name)
        collectJob.cancel()
    }

    @Test
    fun `when SaveRecipe event is triggered should call addRecipeUseCase`() = runTest {
        val collectJob = launch { viewModel.state.collect {} }
        // When
        viewModel.onEvent(
            RecipeEvent.SaveRecipe(
                name = "Bizcocho",
                ingredients = emptyList(),
                yieldQuantity = 1.0,
                yieldUnit = "u."
            )
        )
        advanceUntilIdle()

        // Then
        coVerify { addRecipeUseCase(match { it.name == "Bizcocho" }) }
        collectJob.cancel()
    }

    @Test
    fun `when prepare recipe fails should update error state`() = runTest {
        // Given
        val errorMessage = "Stock insuficiente"
        coEvery { prepareRecipeUseCase(any(), any()) } throws IllegalStateException(errorMessage)
        val recipe = Recipe(name = "Test", ingredients = emptyList())

        val collectJob = launch { viewModel.state.collect {} }

        // When
        viewModel.onEvent(RecipeEvent.PrepareRecipe(recipe))
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        collectJob.cancel()
    }
}
