package com.tiendamuna.stock.domain.util

import com.tiendamuna.stock.domain.model.MeasureUnit

object UnitConverter {
    
    fun convert(amount: Double, fromUnitSymbol: String, toUnitSymbol: String): Double {
        val from = MeasureUnit.fromSymbol(fromUnitSymbol)
        val to = MeasureUnit.fromSymbol(toUnitSymbol)
        
        if (from == to) return amount
        if (from.type != to.type) return amount // Cannot convert between different types
        
        return amount * (from.ratioToBase / to.ratioToBase)
    }

    fun areCompatible(unit1Symbol: String, unit2Symbol: String): Boolean {
        val u1 = MeasureUnit.fromSymbol(unit1Symbol)
        val u2 = MeasureUnit.fromSymbol(unit2Symbol)
        return u1.type == u2.type
    }
}
