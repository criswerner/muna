package com.tiendamuna.stock.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val addRecipeUseCase: AddRecipeUseCase,
    private val prepareRecipeUseCase: PrepareRecipeUseCase,
    private val getStockUseCase: GetStockUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeState())
    val state: StateFlow<RecipeState> = _state.asStateFlow()

    fun loadRecipes() {
        viewModelScope.launch {
            getRecipesUseCase().collect { recipes ->
                _state.value = _state.value.copy(
                    recipes = recipes.map { it.toUiModel() }
                )
            }
        }
    }

    fun loadAvailableIngredients() {
        viewModelScope.launch {
            getStockUseCase().collect { ingredients ->
                _state.value = _state.value.copy(
                    availableIngredients = ingredients.map { it.toUiModel() }
                )
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
                        _state.value = _state.value.copy(error = e.message)
                    }
                }
            }
            is RecipeEvent.PrepareRecipe -> {
                viewModelScope.launch {
                    try {
                        prepareRecipeUseCase(event.recipe)
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(error = e.message)
                    }
                }
            }
            RecipeEvent.ClearError -> {
                _state.value = _state.value.copy(error = null)
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
                        _state.value = _state.value.copy(error = e.message)
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
    data class PrepareRecipe(val recipe: Recipe) : RecipeEvent()
    object ClearError : RecipeEvent()
}
