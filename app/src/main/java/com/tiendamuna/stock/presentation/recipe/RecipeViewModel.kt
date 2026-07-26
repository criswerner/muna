package com.tiendamuna.stock.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.usecase.AddRecipeUseCase
import com.tiendamuna.stock.domain.usecase.DeleteRecipeUseCase
import com.tiendamuna.stock.domain.usecase.GetRecipesUseCase
import com.tiendamuna.stock.domain.usecase.GetStockUseCase
import com.tiendamuna.stock.domain.usecase.PrepareRecipeUseCase
import com.tiendamuna.stock.domain.usecase.UpdateRecipeUseCase
import com.tiendamuna.stock.presentation.recipe.mapper.toUiModel
import com.tiendamuna.stock.presentation.recipe.model.RecipeUiModel
import com.tiendamuna.stock.presentation.stock.mapper.toUiModel
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val addRecipeUseCase: AddRecipeUseCase,
    private val prepareRecipeUseCase: PrepareRecipeUseCase,
    private val getStockUseCase: GetStockUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val _stock = MutableStateFlow<List<Ingredient>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<RecipeState> = combine(
        _recipes,
        _stock,
        _error,
        _isLoading
    ) { recipes, stock, error, isLoading ->
        RecipeState(
            recipes = recipes.map { it.toUiModel(stock) },
            availableIngredients = stock.map { it.toUiModel() },
            isLoading = isLoading,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeState())

    fun loadRecipes() {
        viewModelScope.launch {
            getRecipesUseCase().collect { recipes ->
                _recipes.value = recipes
            }
        }
    }

    fun loadAvailableIngredients() {
        viewModelScope.launch {
            getStockUseCase().collect { ingredients ->
                _stock.value = ingredients
            }
        }
    }

    fun onEvent(event: RecipeEvent) {
        when (event) {
            is RecipeEvent.AddRecipe -> {
                viewModelScope.launch {
                    try {
                        addRecipeUseCase(event.recipe)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            is RecipeEvent.PrepareRecipe -> {
                viewModelScope.launch {
                    try {
                        prepareRecipeUseCase(event.recipe, event.batches)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            RecipeEvent.ClearError -> {
                _error.value = null
            }
            is RecipeEvent.DeleteRecipe -> {
                viewModelScope.launch {
                    deleteRecipeUseCase(event.recipe)
                }
            }
            is RecipeEvent.UpdateRecipe -> {
                viewModelScope.launch {
                    try {
                        updateRecipeUseCase(event.recipe)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
        }
    }
}

data class RecipeState(
    val recipes: List<RecipeUiModel> = emptyList(),
    val availableIngredients: List<IngredientUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RecipeEvent {
    data class AddRecipe(val recipe: Recipe) : RecipeEvent()
    data class UpdateRecipe(val recipe: Recipe) : RecipeEvent()
    data class DeleteRecipe(val recipe: Recipe) : RecipeEvent()
    data class PrepareRecipe(val recipe: Recipe, val batches: Double = 1.0) : RecipeEvent()
    object ClearError : RecipeEvent()
}
