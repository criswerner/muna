package com.tiendamuna.stock.domain.util

object UnitConverter {
    
    private val massUnits = mapOf(
        "kg" to 1000.0,
        "g" to 1.0,
        "mg" to 0.001
    )
    
    private val volumeUnits = mapOf(
        "l" to 1000.0,
        "ml" to 1.0
    )

    fun convert(amount: Double, fromUnit: String, toUnit: String): Double {
        val from = fromUnit.lowercase()
        val to = toUnit.lowercase()
        
        if (from == to) return amount
        
        // Try mass conversion
        if (massUnits.containsKey(from) && massUnits.containsKey(to)) {
            return amount * (massUnits[from]!! / massUnits[to]!!)
        }
        
        // Try volume conversion
        if (volumeUnits.containsKey(from) && volumeUnits.containsKey(to)) {
            return amount * (volumeUnits[from]!! / volumeUnits[to]!!)
        }
        
        // If units are incompatible or unknown, we can't convert automatically
        // but we return the original amount and let the logic decide (likely a mismatch error)
        return amount
    }

    fun areCompatible(unit1: String, unit2: String): Boolean {
        val u1 = unit1.lowercase()
        val u2 = unit2.lowercase()
        if (u1 == u2) return true
        
        val isMass = massUnits.containsKey(u1) && massUnits.containsKey(u2)
        val isVolume = volumeUnits.containsKey(u1) && volumeUnits.containsKey(u2)
        
        return isMass || isVolume
    }
}
