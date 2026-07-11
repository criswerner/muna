package com.tiendamuna.stock.data

import android.content.Context
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class PersistentStockRepositoryImpl(context: Context) : StockRepository {
    private val prefs = context.getSharedPreferences("stock_prefs", Context.MODE_PRIVATE)
    private val _stock = MutableStateFlow<List<Ingredient>>(loadStock())
    
    override fun getStock(): Flow<List<Ingredient>> = _stock.asStateFlow()

    override suspend fun addIngredient(ingredient: Ingredient) {
        _stock.update { currentList ->
            val newList = currentList + ingredient
            saveStock(newList)
            newList
        }
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        _stock.update { currentList ->
            val newList = currentList.map { if (it.id == ingredient.id) ingredient else it }
            saveStock(newList)
            newList
        }
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        _stock.update { currentList ->
            val newList = currentList.filter { it.id != ingredient.id }
            saveStock(newList)
            newList
        }
    }

    private fun saveStock(ingredients: List<Ingredient>) {
        val array = JSONArray()
        ingredients.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("quantity", it.quantity)
            obj.put("unit", it.unit)
            obj.put("category", it.category.name)
            array.put(obj)
        }
        prefs.edit { putString("ingredients_json", array.toString()) }
    }

    private fun loadStock(): List<Ingredient> {
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
                    category = Category.fromName(obj.optString("category", Category.OTHERS.name))
                )
            )
        }
        return list
    }
}
