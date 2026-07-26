package com.tiendamuna.stock.data.datasource.local

import android.content.Context
import androidx.core.content.edit
import com.tiendamuna.stock.data.datasource.HistoryDataSource
import com.tiendamuna.stock.domain.model.PreparationHistory
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

class SharedPrefsHistoryDataSource(context: Context) : HistoryDataSource {
    private val prefs = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)

    override suspend fun getHistory(): List<PreparationHistory> {
        val json = prefs.getString("history_json", null) ?: return emptyList()
        val list = mutableListOf<PreparationHistory>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                PreparationHistory(
                    id = obj.getString("id"),
                    recipeId = obj.getString("recipeId"),
                    recipeName = obj.getString("recipeName"),
                    batchesPrepared = obj.getDouble("batchesPrepared"),
                    totalProducedQuantity = obj.getDouble("totalProducedQuantity"),
                    yieldUnit = obj.getString("yieldUnit"),
                    totalCost = obj.getDouble("totalCost"),
                    timestamp = Date(obj.getLong("timestamp"))
                )
            )
        }
        return list
    }

    override suspend fun saveHistory(history: List<PreparationHistory>) {
        val array = JSONArray()
        history.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("recipeId", it.recipeId)
            obj.put("recipeName", it.recipeName)
            obj.put("batchesPrepared", it.batchesPrepared)
            obj.put("totalProducedQuantity", it.totalProducedQuantity)
            obj.put("yieldUnit", it.yieldUnit)
            obj.put("totalCost", it.totalCost)
            obj.put("timestamp", it.timestamp.time)
            array.put(obj)
        }
        prefs.edit { putString("history_json", array.toString()) }
    }
}
