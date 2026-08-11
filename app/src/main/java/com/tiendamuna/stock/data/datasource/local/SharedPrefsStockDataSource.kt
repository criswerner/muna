package com.tiendamuna.stock.data.datasource.local

import android.content.Context
import androidx.core.content.edit
import com.tiendamuna.stock.data.datasource.StockDataSource
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import org.json.JSONArray
import org.json.JSONObject

class SharedPrefsStockDataSource(context: Context) : StockDataSource {
    private val prefs = context.getSharedPreferences("stock_prefs", Context.MODE_PRIVATE)

    override suspend fun getStock(): List<Ingredient> {
        val json = prefs.getString("ingredients_json", null) ?: return emptyList()
        val list = mutableListOf<Ingredient>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Ingredient(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    quantity = obj.getDouble("quantity"),
                    unit = obj.getString("unit"),
                    category = Category.fromName(obj.optString("category", Category.OTHERS.name)),
                    pricePerUnit = obj.optDouble("pricePerUnit", 0.0),
                    minThreshold = if (obj.has("minThreshold")) obj.getDouble("minThreshold") else null
                )
            )
        }
        return list
    }

    override suspend fun saveIngredients(ingredients: List<Ingredient>) {
        val array = JSONArray()
        ingredients.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("quantity", it.quantity)
            obj.put("unit", it.unit)
            obj.put("category", it.category.name)
            obj.put("pricePerUnit", it.pricePerUnit)
            it.minThreshold?.let { threshold -> obj.put("minThreshold", threshold) }
            array.put(obj)
        }
        prefs.edit { putString("ingredients_json", array.toString()) }
    }
}
