package com.tiendamuna.stock.data.datasource.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tiendamuna.stock.domain.model.PreparationHistory
import kotlinx.coroutines.tasks.await
import java.util.Date

class RemoteHistoryDataSource(private val db: FirebaseFirestore) {

    private val historyCollection = db.collection("history")

    suspend fun getHistory(): Result<List<PreparationHistory>> {
        return try {
            val snapshot = historyCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val entries = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                PreparationHistory(
                    id = doc.id,
                    recipeId = data["recipeId"] as? String ?: "",
                    recipeName = data["recipeName"] as? String ?: "",
                    batchesPrepared = (data["batchesPrepared"] as? Number)?.toDouble() ?: 0.0,
                    totalProducedQuantity = (data["totalProducedQuantity"] as? Number)?.toDouble() ?: 0.0,
                    yieldUnit = data["yieldUnit"] as? String ?: "",
                    totalCost = (data["totalCost"] as? Number)?.toDouble() ?: 0.0,
                    timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                )
            }
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addHistoryEntry(entry: PreparationHistory): Result<Unit> {
        return try {
            val data = hashMapOf(
                "recipeId" to entry.recipeId,
                "recipeName" to entry.recipeName,
                "batchesPrepared" to entry.batchesPrepared,
                "totalProducedQuantity" to entry.totalProducedQuantity,
                "yieldUnit" to entry.yieldUnit,
                "totalCost" to entry.totalCost,
                "timestamp" to entry.timestamp
            )
            historyCollection.document(entry.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
