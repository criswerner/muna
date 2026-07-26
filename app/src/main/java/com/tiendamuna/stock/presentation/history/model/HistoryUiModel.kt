package com.tiendamuna.stock.presentation.history.model

import androidx.compose.runtime.Immutable

@Immutable
data class HistoryUiModel(
    val id: String,
    val recipeName: String,
    val preparationDetail: String, // e.g. "Preparado 2.5 lotes (30 u.)"
    val costDisplay: String,
    val dateDisplay: String
)
