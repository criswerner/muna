package com.tiendamuna.stock.data

import android.content.Context
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

class PersistentRecipeRepositoryImpl(context: Context) : RecipeRepository {
    private val prefs = context.getSharedPreferences("recipe_prefs", Context.MODE_PRIVATE)
    private val _recipes = MutableStateFlow<List<Recipe>>(loadRecipes())
    
    override fun getRecipes(): Flow<List<Recipe>> = _recipes.asStateFlow()

    override suspend fun addRecipe(recipe: Recipe) {
        _recipes.update { currentList ->
            val newList = currentList + recipe
            saveRecipes(newList)
            newList
        }
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        _recipes.update { currentList ->
            val newList = currentList.map { if (it.id == recipe.id) recipe else it }
            saveRecipes(newList)
            newList
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        _recipes.update { list ->
            val newList = list.filter { it.id != recipe.id }
            saveRecipes(newList)
            newList
        }
    }

    private fun saveRecipes(recipes: List<Recipe>) {
        val array = JSONArray()
        recipes.forEach { recipe ->
            val obj = JSONObject()
            obj.put("id", recipe.id)
            obj.put("name", recipe.name)
            obj.put("instructions", recipe.instructions)
            
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
        prefs.edit().putString("recipes_json", array.toString()).apply()
    }

    private fun loadRecipes(): List<Recipe> {
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
                    ingredients = ingredients
                )
            )
        }
        return list
    }
}
