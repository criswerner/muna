package com.tiendamuna.stock.data.datasource.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import kotlinx.coroutines.tasks.await

class RemoteStockDataSource(private val db: FirebaseFirestore) {

    private val ingredientsCollection = db.collection("ingredients")

    suspend fun getStock(): Result<List<Ingredient>> {
        return try {
            val snapshot = ingredientsCollection.get().await()
            val ingredients = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Ingredient(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    quantity = (data["quantity"] as? Number)?.toDouble() ?: 0.0,
                    unit = data["unit"] as? String ?: "",
                    category = Category.valueOf(data["category"] as? String ?: Category.OTHERS.name),
                    pricePerUnit = (data["pricePerUnit"] as? Number)?.toDouble() ?: 0.0
                )
            }
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncStock(ingredients: List<Ingredient>): Result<Unit> {
        return try {
            // This is a simple implementation. In a real app, you might want to use Batches.
            ingredients.forEach { ingredient ->
                addOrUpdateIngredient(ingredient)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrUpdateIngredient(ingredient: Ingredient): Result<Unit> {
        return try {
            val data = hashMapOf(
                "name" to ingredient.name,
                "quantity" to ingredient.quantity,
                "unit" to ingredient.unit,
                "category" to ingredient.category.name,
                "pricePerUnit" to ingredient.pricePerUnit
            )
            ingredientsCollection.document(ingredient.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteIngredient(ingredientId: String): Result<Unit> {
        return try {
            ingredientsCollection.document(ingredientId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
