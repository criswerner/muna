package com.tiendamuna.stock.domain.model

data class Ingredient(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: Category = Category.OTHERS,
    val pricePerUnit: Double = 0.0,
    val minThreshold: Double? = null
)
