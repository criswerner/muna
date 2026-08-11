package com.tiendamuna.stock.presentation.history.mapper

import com.tiendamuna.stock.domain.model.PreparationHistory
import com.tiendamuna.stock.presentation.history.model.HistoryUiModel
import java.text.SimpleDateFormat
import java.util.Locale

fun PreparationHistory.toUiModel(): HistoryUiModel {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val batchesFormatted = String.format(Locale.getDefault(), "%.2f", batchesPrepared)
    val totalQtyFormatted = String.format(Locale.getDefault(), "%.2f", totalProducedQuantity)
    
    return HistoryUiModel(
        id = id,
        recipeName = recipeName,
        preparationDetail = "Preparado $batchesFormatted lotes ($totalQtyFormatted $yieldUnit)",
        costDisplay = "$${String.format(Locale.getDefault(), "%,.2f", totalCost)}",
        dateDisplay = dateFormat.format(timestamp)
    )
}
