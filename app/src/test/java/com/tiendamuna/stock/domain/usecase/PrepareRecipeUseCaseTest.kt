package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class PrepareRecipeUseCaseTest {

    private lateinit var stockRepository: StockRepository
    private lateinit var useCase: PrepareRecipeUseCase

    @Before
    fun setUp() {
        stockRepository = mockk(relaxed = true)
        useCase = PrepareRecipeUseCase(stockRepository)
    }

    @Test
    fun `when batches is zero or less should throw exception`() = runTest {
        val recipe = mockk<Recipe>()
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(recipe, batches = 0.0)
            }
        }
    }

    @Test
    fun `when stock is insufficient should throw exception`() = runTest {
        // Given
        val ingredientId = "1"
        val recipe = Recipe(
            name = "Test Recipe",
            ingredients = listOf(
                RecipeIngredient(ingredientId, "Ingrediente 1", 100.0, "g")
            )
        )
        val stockItem = Ingredient(id = ingredientId, name = "Ingrediente 1", quantity = 50.0, unit = "g")
        coEvery { stockRepository.getStock() } returns flowOf(listOf(stockItem))

        // Then
        assertThrows(IllegalStateException::class.java) {
            runTest {
                useCase(recipe, batches = 1.0)
            }
        }
    }

    @Test
    fun `when stock is sufficient should update stock for all ingredients`() = runTest {
        // Given
        val id1 = "1"
        val id2 = "2"
        val recipe = Recipe(
            name = "Test Recipe",
            ingredients = listOf(
                RecipeIngredient(id1, "Ingrediente 1", 100.0, "g"),
                RecipeIngredient(id2, "Ingrediente 2", 200.0, "ml")
            )
        )
        val stock1 = Ingredient(id = id1, name = "Ingrediente 1", quantity = 500.0, unit = "g")
        val stock2 = Ingredient(id = id2, name = "Ingrediente 2", quantity = 1000.0, unit = "ml")
        
        coEvery { stockRepository.getStock() } returns flowOf(listOf(stock1, stock2))

        // When
        useCase(recipe, batches = 2.0)

        // Then
        coVerify {
            stockRepository.updateIngredient(match { it.id == id1 && it.quantity == 300.0 })
            stockRepository.updateIngredient(match { it.id == id2 && it.quantity == 600.0 })
        }
    }
}
