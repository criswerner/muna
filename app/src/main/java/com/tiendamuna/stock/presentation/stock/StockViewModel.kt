package com.tiendamuna.stock.presentation.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.usecase.AddIngredientUseCase
import com.tiendamuna.stock.domain.usecase.DeleteIngredientUseCase
import com.tiendamuna.stock.domain.usecase.GetStockUseCase
import com.tiendamuna.stock.domain.usecase.UpdateIngredientUseCase
import com.tiendamuna.stock.presentation.stock.mapper.toUiModel
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockViewModel(
    private val getStockUseCase: GetStockUseCase,
    private val addIngredientUseCase: AddIngredientUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase,
    private val deleteIngredientUseCase: DeleteIngredientUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<StockState>(StockState())
    val state: StateFlow<StockState> = _state.asStateFlow()

    init {
        loadStock()
    }

    private fun loadStock() {
        viewModelScope.launch {
            getStockUseCase().collect { ingredients ->
                _state.value = _state.value.copy(
                    ingredients = ingredients.map { it.toUiModel() }
                )
            }
        }
    }

    fun onEvent(event: StockEvent) {
        when (event) {
            is StockEvent.AddIngredient -> {
                viewModelScope.launch {
                    try {
                        addIngredientUseCase(
                            name = event.name,
                            quantity = event.quantity,
                            unit = event.unit
                        )
                        _state.value = _state.value.copy(error = null)
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(error = e.message)
                    }
                }
            }
            StockEvent.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
            is StockEvent.DeleteIngredient -> {
                viewModelScope.launch {
                    deleteIngredientUseCase(event.ingredient)
                }
            }
            is StockEvent.UpdateIngredient -> {
                viewModelScope.launch {
                    try {
                        updateIngredientUseCase(event.ingredient)
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(error = e.message)
                    }
                }
            }
        }
    }
}

data class StockState(
    val ingredients: List<IngredientUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class StockEvent {
    data class AddIngredient(val name: String, val quantity: Double, val unit: String) : StockEvent()
    data class UpdateIngredient(val ingredient: Ingredient) : StockEvent()
    data class DeleteIngredient(val ingredient: Ingredient) : StockEvent()
    object ClearError : StockEvent()
}
