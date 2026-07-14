package com.tiendamuna.stock.domain.model

enum class UnitType {
    MASS, VOLUME, COUNT
}

enum class MeasureUnit(val symbol: String, val type: UnitType, val ratioToBase: Double) {
    // Mass (Base: gram)
    KILOGRAM("kg", UnitType.MASS, 1000.0),
    GRAM("g", UnitType.MASS, 1.0),
    MILLIGRAM("mg", UnitType.MASS, 0.001),
    
    // Volume (Base: milliliter)
    LITER("l", UnitType.VOLUME, 1000.0),
    MILLILITER("ml", UnitType.VOLUME, 1.0),
    
    // Count (Base: unit)
    UNIT("u.", UnitType.COUNT, 1.0);

    companion object {
        fun fromSymbol(symbol: String): MeasureUnit {
            return entries.find { it.symbol.equals(symbol, ignoreCase = true) } ?: UNIT
        }
        
        fun getAllSymbols(): List<String> = entries.map { it.symbol }
    }
}
