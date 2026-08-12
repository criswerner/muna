package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.DomainException
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

class UpdateIngredientUseCaseTest {

    private lateinit var repository: StockRepository
    private lateinit var useCase: UpdateIngredientUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = UpdateIngredientUseCase(repository)
    }

    @Test
    fun `when name is blank should throw exception`() = runTest {
        val ingredient = Ingredient(id = "1", name = "", quantity = 10.0, unit = "g")
        assertThrows(DomainException.EmptyName::class.java) {
            runBlocking { useCase(ingredient) }
        }
    }

    @Test
    fun `when quantity is negative should throw exception`() = runTest {
        val ingredient = Ingredient(id = "1", name = "Harina", quantity = -1.0, unit = "g")
        assertThrows(DomainException.InvalidQuantity::class.java) {
            runBlocking { useCase(ingredient) }
        }
    }

    @Test
    fun `when name is already taken by another ingredient should throw exception`() = runTest {
        // Given
        val ingredient = Ingredient(id = "1", name = "Harina", quantity = 10.0, unit = "g")
        val other = Ingredient(id = "2", name = "Harina", quantity = 5.0, unit = "g")
        coEvery { repository.getStock() } returns flowOf(listOf(other))

        // Then
        assertThrows(DomainException.NameAlreadyTaken::class.java) {
            runBlocking { useCase(ingredient) }
        }
    }

    @Test
    fun `when data is valid should call updateIngredient`() = runTest {
        // Given
        val ingredient = Ingredient(id = "1", name = "Harina", quantity = 10.0, unit = "g")
        coEvery { repository.getStock() } returns flowOf(emptyList())

        // When
        useCase(ingredient)

        // Then
        coVerify { repository.updateIngredient(ingredient) }
    }
}
