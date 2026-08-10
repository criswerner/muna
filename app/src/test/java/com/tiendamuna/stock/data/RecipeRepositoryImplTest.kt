package com.tiendamuna.stock.data

import com.tiendamuna.stock.data.datasource.RecipeDataSource
import com.tiendamuna.stock.data.datasource.remote.RemoteRecipeDataSource
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.service.RecipeIntegrityService
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
class RecipeRepositoryImplTest {

    private lateinit var localDataSource: RecipeDataSource
    private lateinit var remoteDataSource: RemoteRecipeDataSource
    private lateinit var integrityService: RecipeIntegrityService
    private lateinit var repository: RecipeRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        remoteDataSource = mockk(relaxed = true)
        integrityService = mockk(relaxed = true)
        repository = RecipeRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            integrityService = integrityService,
            externalScope = testScope,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getRecipes should load from local and then remote when not loaded`() = runTest(testDispatcher) {
        // Given
        val localRecipes = listOf(mockk<Recipe>(relaxed = true))
        val remoteRecipes = listOf(mockk<Recipe>(relaxed = true), mockk(relaxed = true))
        coEvery { localDataSource.getRecipes() } returns localRecipes
        coEvery { remoteDataSource.getRecipes() } returns Result.success(remoteRecipes)

        val results = mutableListOf<List<Recipe>>()
        val job = launch {
            repository.getRecipes().collect { results.add(it) }
        }

        // When
        advanceUntilIdle()

        // Then
        assertEquals(remoteRecipes, results.last())
        coVerify { localDataSource.saveRecipes(remoteRecipes) }
        job.cancel()
    }

    @Test
    fun `addRecipe should update local and sync to remote`() = runTest(testDispatcher) {
        // Given
        val recipe = Recipe(name = "New Recipe", ingredients = emptyList(), instructions = "")
        
        // When
        repository.addRecipe(recipe)
        advanceUntilIdle()

        // Then
        coVerify { localDataSource.saveRecipes(match { it.contains(recipe) }) }
        coVerify { remoteDataSource.addOrUpdateRecipe(recipe) }
    }

    @Test
    fun `updateRecipe should update local and sync to remote and call integrity service if name changed`() = runTest(testDispatcher) {
        // Given
        val oldRecipe = Recipe(id = "1", name = "Old Name", ingredients = emptyList(), instructions = "")
        val newRecipe = Recipe(id = "1", name = "New Name", ingredients = emptyList(), instructions = "")
        
        coEvery { localDataSource.getRecipes() } returns listOf(oldRecipe)
        coEvery { remoteDataSource.getRecipes() } returns Result.success(listOf(oldRecipe))
        
        // Initial load
        val job = launch { repository.getRecipes().collect {} }
        advanceUntilIdle()

        // When
        repository.updateRecipe(newRecipe)
        advanceUntilIdle()

        // Then
        coVerify { remoteDataSource.addOrUpdateRecipe(newRecipe) }
        coVerify { integrityService.propagateRecipeNameChange(newRecipe) }
        job.cancel()
    }

    @Test
    fun `deleteRecipe should update local and sync to remote`() = runTest(testDispatcher) {
        // Given
        val recipe = Recipe(id = "1", name = "Delete Me", ingredients = emptyList(), instructions = "")
        
        // When
        repository.deleteRecipe(recipe)
        advanceUntilIdle()

        // Then
        coVerify { localDataSource.saveRecipes(match { !it.contains(recipe) }) }
        coVerify { remoteDataSource.deleteRecipe(recipe.id) }
    }
}
