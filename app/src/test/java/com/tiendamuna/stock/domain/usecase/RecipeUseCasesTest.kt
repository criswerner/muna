package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.DomainException
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.repository.RecipeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class RecipeUseCasesTest {

    private lateinit var repository: RecipeRepository
    
    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `AddRecipeUseCase should throw exception if name is blank`() = runTest {
        val useCase = AddRecipeUseCase(repository)
        val recipe = Recipe(name = "", ingredients = listOf(mockk()))
        
        assertThrows(DomainException.EmptyName::class.java) {
            runBlocking { useCase(recipe) }
        }
    }

    @Test
    fun `AddRecipeUseCase should throw exception if ingredients are empty`() = runTest {
        val useCase = AddRecipeUseCase(repository)
        val recipe = Recipe(name = "Pan", ingredients = emptyList())
        
        assertThrows(DomainException.NoIngredientsInRecipe::class.java) {
            runBlocking { useCase(recipe) }
        }
    }

    @Test
    fun `AddRecipeUseCase should call repository when valid`() = runTest {
        val useCase = AddRecipeUseCase(repository)
        val recipe = Recipe(name = "Pan", ingredients = listOf(mockk()))
        
        useCase(recipe)
        
        coVerify { repository.addRecipe(recipe) }
    }

    @Test
    fun `UpdateRecipeUseCase should call repository`() = runTest {
        val useCase = UpdateRecipeUseCase(repository)
        val recipe = Recipe(name = "Pan", ingredients = listOf(mockk()))
        
        useCase(recipe)
        
        coVerify { repository.updateRecipe(recipe) }
    }

    @Test
    fun `DeleteRecipeUseCase should call repository`() = runTest {
        val useCase = DeleteRecipeUseCase(repository)
        val recipe = Recipe(name = "Pan", ingredients = listOf(mockk()))
        
        useCase(recipe)
        
        coVerify { repository.deleteRecipe(recipe) }
    }

    @Test
    fun `GetRecipesUseCase should return flow from repository`() = runTest {
        val useCase = GetRecipesUseCase(repository)
        val recipes = listOf(Recipe(name = "Pan", ingredients = emptyList()))
        coEvery { repository.getRecipes() } returns flowOf(recipes)
        
        val result = mutableListOf<List<Recipe>>()
        useCase().collect { result.add(it) }
        
        assert(result[0] == recipes)
    }
}
