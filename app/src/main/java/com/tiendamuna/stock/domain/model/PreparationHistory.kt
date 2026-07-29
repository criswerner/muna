package com.tiendamuna.stock.domain.model

import java.util.Date
import java.util.UUID

data class PreparationHistory(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val recipeName: String,
    val batchesPrepared: Double,
    val totalProducedQuantity: Double,
    val yieldUnit: String,
    val totalCost: Double,
    val timestamp: Date = Date()
)
