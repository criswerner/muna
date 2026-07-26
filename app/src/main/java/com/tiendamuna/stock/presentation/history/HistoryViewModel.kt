package com.tiendamuna.stock.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiendamuna.stock.domain.usecase.GetHistoryUseCase
import com.tiendamuna.stock.presentation.history.mapper.toUiModel
import com.tiendamuna.stock.presentation.history.model.HistoryUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    val state: StateFlow<HistoryState> = getHistoryUseCase()
        .map { history ->
            HistoryState(entries = history.map { it.toUiModel() })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryState(isLoading = true)
        )
}

data class HistoryState(
    val entries: List<HistoryUiModel> = emptyList(),
    val isLoading: Boolean = false
)
