package com.tiendamuna.stock.domain.service

import com.tiendamuna.stock.domain.model.Ingredient

/**
 * Service to handle data integrity related to Ingredients across different domains.
 */
interface IngredientIntegrityService {
    /**
     * Propagates a change in an ingredient's name to all its references (e.g., Recipes).
     */
    suspend fun propagateIngredientNameChange(ingredient: Ingredient)
}
