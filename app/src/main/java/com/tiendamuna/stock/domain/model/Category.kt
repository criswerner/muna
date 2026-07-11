package com.tiendamuna.stock.domain.model

enum class Category(val displayName: String) {
    DAIRY("Lácteos"),
    VEGETABLES("Verduras"),
    MEATS("Carnes"),
    PANTRY("Despensa"),
    FRUITS("Frutas"),
    OTHERS("Otros");

    companion object {
        fun fromName(name: String): Category {
            return entries.find { it.name == name } ?: OTHERS
        }
    }
}
