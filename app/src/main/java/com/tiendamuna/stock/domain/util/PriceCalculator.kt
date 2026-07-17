package com.tiendamuna.stock.domain.util

object PriceCalculator {

    /**
     * Calculates the new weighted average unit price when adding a new batch of stock.
     */
    fun calculateWeightedAveragePrice(
        currentQuantity: Double,
        currentUnitPrice: Double,
        addedQuantity: Double,
        totalAddedPrice: Double
    ): Double {
        val totalQuantity = currentQuantity + addedQuantity
        if (totalQuantity <= 0) return 0.0
        
        val currentTotalValue = currentQuantity * currentUnitPrice
        return (currentTotalValue + totalAddedPrice) / totalQuantity
    }
}
