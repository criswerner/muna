package com.tiendamuna.stock.presentation.history.mapper

import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.presentation.history.model.HistoryUiModel
import java.text.SimpleDateFormat
import java.util.Locale

fun PreparationHistory.toUiModel(): HistoryUiModel {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    return HistoryUiModel(
        id = id,
        recipeName = recipeName,
        batchesPrepared = String.format(Locale.getDefault(), "%.2f", batchesPrepared),
        totalProducedQuantity = String.format(Locale.getDefault(), "%.2f", totalProducedQuantity),
        yieldUnit = yieldUnit,
        costDisplay = "$${String.format(Locale.getDefault(), "%,.2f", totalCost)}",
        dateDisplay = dateFormat.format(timestamp)
    )
}
