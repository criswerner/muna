package com.tiendamuna.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tiendamuna.stock.navigation.NavigationItem
import com.tiendamuna.stock.navigation.StockBottomNavigation
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
                                container.deleteIngredientUseCase,
                                container.errorMessageHelper
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
                                container.addHistoryEntryUseCase,
                                container.errorMessageHelper
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
                        val mainScreens = listOf("stock", "recipes", "history")
                        if (currentRoute in mainScreens) {
                            val navItems = listOf(
                                NavigationItem(
                                    route = "stock",
                                    icon = Icons.Default.ShoppingCart,
                                    label = stringResource(R.string.title_stock)
                                ),
                                NavigationItem(
                                    route = "recipes",
                                    icon = Icons.AutoMirrored.Filled.List,
                                    label = stringResource(R.string.title_recipes)
                                ),
                                NavigationItem(
                                    route = "history",
                                    icon = Icons.Default.History,
                                    label = stringResource(R.string.title_history)
                                )
                            )
                            StockBottomNavigation(
                                currentRoute = currentRoute,
                                navController = navController,
                                items = navItems
                            )
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
