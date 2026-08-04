package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StockUseCasesTest {

    private lateinit var repository: StockRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `GetStockUseCase should return flow from repository`() = runTest {
        val useCase = GetStockUseCase(repository)
        val ingredients = listOf(Ingredient(name = "Test", quantity = 10.0, unit = "g"))
        coEvery { repository.getStock() } returns flowOf(ingredients)

        val result = mutableListOf<List<Ingredient>>()
        useCase().collect { result.add(it) }

        assertEquals(ingredients, result[0])
    }

    @Test
    fun `DeleteIngredientUseCase should call repository delete`() = runTest {
        val useCase = DeleteIngredientUseCase(repository)
        val ingredient = Ingredient(name = "Test", quantity = 10.0, unit = "g")

        useCase(ingredient)

        coVerify { repository.deleteIngredient(ingredient) }
    }
}
