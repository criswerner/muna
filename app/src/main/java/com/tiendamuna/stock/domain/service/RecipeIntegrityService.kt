package com.tiendamuna.stock.domain.service

import com.tiendamuna.stock.domain.model.Recipe

/**
 * Service to handle data integrity related to Recipes across different domains.
 */
interface RecipeIntegrityService {
    /**
     * Propagates a change in a recipe's name to all its references (e.g., History).
     */
    suspend fun propagateRecipeNameChange(recipe: Recipe)
}
