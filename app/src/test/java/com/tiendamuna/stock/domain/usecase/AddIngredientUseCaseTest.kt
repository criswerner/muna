package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.Category
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

class AddIngredientUseCaseTest {

    private lateinit var repository: StockRepository
    private lateinit var useCase: AddIngredientUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = AddIngredientUseCase(repository)
    }

    @Test
    fun `when name is blank should throw exception`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(name = "", quantity = 10.0, unit = "g")
            }
        }
    }

    @Test
    fun `when quantity is zero or less should throw exception`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(name = "Harina", quantity = 0.0, unit = "g")
            }
        }
    }

    @Test
    fun `when ingredient does not exist should call addIngredient`() = runTest {
        // Given
        coEvery { repository.getStock() } returns flowOf(emptyList())

        // When
        useCase(name = "Harina", quantity = 100.0, unit = "g", category = Category.OTHERS, totalPrice = 500.0)

        // Then
        coVerify { 
            repository.addIngredient(match { 
                it.name == "Harina" && it.quantity == 100.0 && it.pricePerUnit == 5.0 
            }) 
        }
    }

    @Test
    fun `when ingredient exists and unit is compatible should call updateIngredient`() = runTest {
        // Given
        val existing = Ingredient(name = "Harina", quantity = 50.0, unit = "g", pricePerUnit = 2.0)
        coEvery { repository.getStock() } returns flowOf(listOf(existing))

        // When
        useCase(name = "Harina", quantity = 50.0, unit = "g", totalPrice = 200.0)

        // Then
        coVerify {
            repository.updateIngredient(match {
                it.name == "Harina" && it.quantity == 100.0 && it.pricePerUnit == 3.0
            })
        }
    }

    @Test
    fun `when ingredient exists but unit is incompatible should throw exception`() = runTest {
        // Given
        val existing = Ingredient(name = "Harina", quantity = 50.0, unit = "g")
        coEvery { repository.getStock() } returns flowOf(listOf(existing))

        // Then
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(name = "Harina", quantity = 1.0, unit = "l")
            }
        }
    }
}
