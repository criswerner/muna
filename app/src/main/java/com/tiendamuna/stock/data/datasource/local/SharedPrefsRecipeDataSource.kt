package com.tiendamuna.stock.data.datasource.local

import android.content.Context
import androidx.core.content.edit
import com.tiendamuna.stock.data.datasource.RecipeDataSource
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import org.json.JSONArray
import org.json.JSONObject

class SharedPrefsRecipeDataSource(context: Context) : RecipeDataSource {
    private val prefs = context.getSharedPreferences("recipe_prefs", Context.MODE_PRIVATE)

    override suspend fun getRecipes(): List<Recipe> {
        val json = prefs.getString("recipes_json", null) ?: return emptyList()
        val list = mutableListOf<Recipe>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val ingredients = mutableListOf<RecipeIngredient>()
            val ingArray = obj.getJSONArray("ingredients")
            for (j in 0 until ingArray.length()) {
                val ingObj = ingArray.getJSONObject(j)
                ingredients.add(
                    RecipeIngredient(
                        ingredientId = ingObj.getString("ingredientId"),
                        name = ingObj.getString("name"),
                        quantityRequired = ingObj.getDouble("quantityRequired"),
                        unit = ingObj.getString("unit")
                    )
                )
            }
            list.add(
                Recipe(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    instructions = obj.optString("instructions", ""),
                    ingredients = ingredients,
                    yieldQuantity = obj.optDouble("yieldQuantity", 1.0),
                    yieldUnit = obj.optString("yieldUnit", "u.")
                )
            )
        }
        return list
    }

    override suspend fun saveRecipes(recipes: List<Recipe>) {
        val array = JSONArray()
        recipes.forEach { recipe ->
            val obj = JSONObject()
            obj.put("id", recipe.id)
            obj.put("name", recipe.name)
            obj.put("instructions", recipe.instructions)
            obj.put("yieldQuantity", recipe.yieldQuantity)
            obj.put("yieldUnit", recipe.yieldUnit)
            
            val ingredientsArray = JSONArray()
            recipe.ingredients.forEach { ing ->
                val ingObj = JSONObject()
                ingObj.put("ingredientId", ing.ingredientId)
                ingObj.put("name", ing.name)
                ingObj.put("quantityRequired", ing.quantityRequired)
                ingObj.put("unit", ing.unit)
                ingredientsArray.put(ingObj)
            }
            obj.put("ingredients", ingredientsArray)
            array.put(obj)
        }
        prefs.edit { putString("recipes_json", array.toString()) }
    }
}
