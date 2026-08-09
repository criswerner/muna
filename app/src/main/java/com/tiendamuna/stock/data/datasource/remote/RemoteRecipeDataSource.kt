package com.tiendamuna.stock.data.datasource.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import kotlinx.coroutines.tasks.await

class RemoteRecipeDataSource(private val db: FirebaseFirestore) {

    private val recipesCollection = db.collection("recipes")

    suspend fun getRecipes(): Result<List<Recipe>> {
        return try {
            val snapshot = recipesCollection.get().await()
            val recipes = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                
                @Suppress("UNCHECKED_CAST")
                val ingredientsList = (data["ingredients"] as? List<Map<String, Any>>) ?: emptyList()
                
                Recipe(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    ingredients = ingredientsList.map { ingData ->
                        RecipeIngredient(
                            ingredientId = ingData["ingredientId"] as? String ?: "",
                            name = ingData["name"] as? String ?: "",
                            quantityRequired = (ingData["quantityRequired"] as? Number)?.toDouble() ?: 0.0,
                            unit = ingData["unit"] as? String ?: ""
                        )
                    },
                    instructions = data["instructions"] as? String ?: "",
                    yieldQuantity = (data["yieldQuantity"] as? Number)?.toDouble() ?: 1.0,
                    yieldUnit = data["yieldUnit"] as? String ?: ""
                )
            }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrUpdateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            val data = hashMapOf(
                "name" to recipe.name,
                "instructions" to recipe.instructions,
                "yieldQuantity" to recipe.yieldQuantity,
                "yieldUnit" to recipe.yieldUnit,
                "ingredients" to recipe.ingredients.map { ing ->
                    hashMapOf(
                        "ingredientId" to ing.ingredientId,
                        "name" to ing.name,
                        "quantityRequired" to ing.quantityRequired,
                        "unit" to ing.unit
                    )
                }
            )
            recipesCollection.document(recipe.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
