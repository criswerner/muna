package com.tiendamuna.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tiendamuna.stock.presentation.recipe.RecipeScreen
import com.tiendamuna.stock.presentation.recipe.RecipeViewModel
import com.tiendamuna.stock.presentation.stock.StockScreen
import com.tiendamuna.stock.presentation.stock.StockViewModel
import com.tiendamuna.stock.ui.theme.StockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the dependency container from Application
        val container = (application as StockApplication).container

        enableEdgeToEdge()
        setContent {
            StockTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                
                val stockViewModel: StockViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return StockViewModel(
                                container.getStockUseCase, 
                                container.addIngredientUseCase,
                                container.updateIngredientUseCase,
                                container.deleteIngredientUseCase
                            ) as T
                        }
                    }
                )
                
                val recipeViewModel: RecipeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return RecipeViewModel(
                                container.getRecipesUseCase, 
                                container.addRecipeUseCase, 
                                container.prepareRecipeUseCase,
                                container.getStockUseCase,
                                container.updateRecipeUseCase,
                                container.deleteRecipeUseCase
                            ) as T
                        }
                    }
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Stock") },
                                label = { Text("Stock") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.List, contentDescription = "Recetas") },
                                label = { Text("Recetas") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> StockScreen(viewModel = stockViewModel)
                            1 -> RecipeScreen(viewModel = recipeViewModel)
                        }
                    }
                }
            }
        }
    }
}
