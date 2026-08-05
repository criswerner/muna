package com.tiendamuna.stock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tiendamuna.stock.presentation.history.HistoryScreen
import com.tiendamuna.stock.presentation.history.HistoryViewModel
import com.tiendamuna.stock.presentation.recipe.RecipeDetailScreen
import com.tiendamuna.stock.presentation.recipe.RecipeFormScreen
import com.tiendamuna.stock.presentation.recipe.RecipeScreen
import com.tiendamuna.stock.presentation.recipe.RecipeViewModel
import com.tiendamuna.stock.presentation.stock.StockScreen
import com.tiendamuna.stock.presentation.stock.StockViewModel

@Composable
fun StockNavGraph(
    navController: NavHostController,
    stockViewModel: StockViewModel,
    recipeViewModel: RecipeViewModel,
    historyViewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "stock",
        modifier = modifier
    ) {
        composable("stock") {
            StockScreen(viewModel = stockViewModel)
        }
        composable("recipes") {
            RecipeScreen(
                viewModel = recipeViewModel,
                onNavigateToCreate = { navController.navigate("recipe_form") },
                onNavigateToEdit = { id -> navController.navigate("recipe_form?recipeId=$id") },
                onNavigateToDetail = { id -> navController.navigate("recipe_detail/$id") }
            )
        }
        composable(
            route = "recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(
                viewModel = recipeViewModel,
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> 
                    navController.navigate("recipe_form?recipeId=$id") {
                        popUpTo("recipes")
                    }
                }
            )
        }
        composable("history") {
            HistoryScreen(viewModel = historyViewModel)
        }
        composable(
            route = "recipe_form?recipeId={recipeId}",
            arguments = listOf(
                navArgument("recipeId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            RecipeFormScreen(
                viewModel = recipeViewModel,
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
