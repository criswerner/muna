package com.tiendamuna.stock.presentation.common.mapper

import android.content.Context
import com.tiendamuna.stock.R
import com.tiendamuna.stock.domain.model.DomainException

class ErrorMessageHelper(private val context: Context) {
    fun getMessage(exception: Throwable): String {
        return when (exception) {
            is DomainException.EmptyName -> context.getString(R.string.error_empty_name)
            is DomainException.InvalidQuantity -> context.getString(R.string.error_invalid_quantity)
            is DomainException.IncompatibleUnits -> context.getString(
                R.string.error_incompatible_units,
                exception.ingredientName,
                exception.unit
            )
            is DomainException.NoIngredientsInRecipe -> context.getString(R.string.error_no_ingredients)
            is DomainException.NameAlreadyTaken -> context.getString(
                R.string.error_name_already_taken,
                exception.name
            )
            is DomainException.StockInsufficient -> context.getString(R.string.error_insufficient_stock)
            else -> exception.message ?: context.getString(R.string.error_unknown)
        }
    }
}
