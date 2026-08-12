package com.tiendamuna.stock.presentation.history.model

import androidx.compose.runtime.Immutable

@Immutable
data class HistoryUiModel(
    val id: String,
    val recipeName: String,
    val batchesPrepared: String,
    val totalProducedQuantity: String,
    val yieldUnit: String,
    val costDisplay: String,
    val dateDisplay: String
)
