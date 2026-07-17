package com.tiendamuna.stock.presentation.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.usecase.AddIngredientUseCase
import com.tiendamuna.stock.domain.usecase.DeleteIngredientUseCase
import com.tiendamuna.stock.domain.usecase.GetStockUseCase
import com.tiendamuna.stock.domain.usecase.UpdateIngredientUseCase
import com.tiendamuna.stock.presentation.stock.mapper.toUiModel
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(
    private val getStockUseCase: GetStockUseCase,
    private val addIngredientUseCase: AddIngredientUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase,
    private val deleteIngredientUseCase: DeleteIngredientUseCase
) : ViewModel() {

    private val _ingredients = MutableStateFlow<List<IngredientUiModel>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<StockState> = combine(
        _ingredients,
        _searchQuery,
        _error,
        _isLoading
    ) { ingredients, query, error, isLoading ->
        StockState(
            ingredients = if (query.isBlank()) {
                ingredients
            } else {
                ingredients.filter { it.name.contains(query, ignoreCase = true) }
            },
            searchQuery = query,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StockState())

    init {
        loadStock()
    }

    private fun loadStock() {
        viewModelScope.launch {
            getStockUseCase().collect { ingredients ->
                _ingredients.value = ingredients.map { it.toUiModel() }
            }
        }
    }

    fun onEvent(event: StockEvent) {
        when (event) {
            is StockEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is StockEvent.AddIngredient -> {
                viewModelScope.launch {
                    try {
                        addIngredientUseCase(
                            name = event.name,
                            quantity = event.quantity,
                            unit = event.unit,
                            category = event.category,
                            totalPrice = event.totalPrice
                        )
                        _error.value = null
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            StockEvent.ClearError -> {
                _error.value = null
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
                        _error.value = e.message
                    }
                }
            }
        }
    }
}

data class StockState(
    val ingredients: List<IngredientUiModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class StockEvent {
    data class SearchQueryChanged(val query: String) : StockEvent()
    data class AddIngredient(
        val name: String, 
        val quantity: Double, 
        val unit: String, 
        val category: Category,
        val totalPrice: Double
    ) : StockEvent()
    data class UpdateIngredient(val ingredient: Ingredient) : StockEvent()
    data class DeleteIngredient(val ingredient: Ingredient) : StockEvent()
    object ClearError : StockEvent()
}
