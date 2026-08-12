package com.tiendamuna.stock.domain.model

sealed class DomainException : Exception() {
    object EmptyName : DomainException()
    object InvalidQuantity : DomainException()
    data class IncompatibleUnits(val ingredientName: String, val unit: String) : DomainException()
    object NoIngredientsInRecipe : DomainException()
    data class NameAlreadyTaken(val name: String) : DomainException()
    object StockInsufficient : DomainException()
}
