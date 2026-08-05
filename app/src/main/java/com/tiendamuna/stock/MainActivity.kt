package com.tiendamuna.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tiendamuna.stock.navigation.StockNavGraph
import com.tiendamuna.stock.presentation.history.HistoryViewModel
import com.tiendamuna.stock.presentation.recipe.RecipeViewModel
import com.tiendamuna.stock.presentation.stock.StockViewModel
import com.tiendamuna.stock.ui.theme.StockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Get the dependency container from Application
        val container = (application as StockApplication).container

        enableEdgeToEdge()
        setContent {
            StockTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

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
                                container.deleteRecipeUseCase,
                                container.addHistoryEntryUseCase
                            ) as T
                        }
                    }
                )

                val historyViewModel: HistoryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HistoryViewModel(container.getHistoryUseCase) as T
                        }
                    }
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Only show bottom bar on main screens
                        if (currentRoute == "stock" || currentRoute == "recipes" || currentRoute == "history") {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "stock",
                                    onClick = { 
                                        if (currentRoute != "stock") {
                                            navController.navigate("stock") {
                                                popUpTo("stock") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Stock") },
                                    label = { Text(stringResource(R.string.title_stock)) }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "recipes",
                                    onClick = { 
                                        if (currentRoute != "recipes") {
                                            navController.navigate("recipes") {
                                                popUpTo("stock") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Recetas") },
                                    label = { Text(stringResource(R.string.title_recipes)) }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "history",
                                    onClick = { 
                                        if (currentRoute != "history") {
                                            navController.navigate("history") {
                                                popUpTo("stock") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                                    label = { Text("Historial") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    StockNavGraph(
                        navController = navController,
                        stockViewModel = stockViewModel,
                        recipeViewModel = recipeViewModel,
                        historyViewModel = historyViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
