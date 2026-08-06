package com.tiendamuna.stock.presentation.stock

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.usecase.AddIngredientUseCase
import com.tiendamuna.stock.domain.usecase.DeleteIngredientUseCase
import com.tiendamuna.stock.domain.usecase.GetStockUseCase
import com.tiendamuna.stock.domain.usecase.UpdateIngredientUseCase
import com.tiendamuna.stock.navigation.StockNavGraph
import com.tiendamuna.stock.presentation.history.HistoryViewModel
import com.tiendamuna.stock.presentation.recipe.RecipeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StockIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val getStockUseCase: GetStockUseCase = mockk()
    private val addIngredientUseCase: AddIngredientUseCase = mockk(relaxed = true)
    private val updateIngredientUseCase: UpdateIngredientUseCase = mockk(relaxed = true)
    private val deleteIngredientUseCase: DeleteIngredientUseCase = mockk(relaxed = true)
    
    // Other ViewModels mocked as they are needed for NavGraph
    private val recipeViewModel: RecipeViewModel = mockk(relaxed = true)
    private val historyViewModel: HistoryViewModel = mockk(relaxed = true)

    private lateinit var stockViewModel: StockViewModel

    @Before
    fun setUp() {
        coEvery { getStockUseCase() } returns flowOf(
            listOf(
                Ingredient(id = "1", name = "Harina", quantity = 10.0, unit = "kg", category = Category.OTHERS, pricePerUnit = 50.0)
            )
        )
        
        stockViewModel = StockViewModel(
            getStockUseCase,
            addIngredientUseCase,
            updateIngredientUseCase,
            deleteIngredientUseCase
        )
    }

    @Test
    fun stockScreen_showsListAndNavigatesToAddIngredient() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            StockNavGraph(
                navController = navController,
                stockViewModel = stockViewModel,
                recipeViewModel = recipeViewModel,
                historyViewModel = historyViewModel
            )
        }

        // Check if Harina is shown
        composeTestRule.onNodeWithText("Harina", ignoreCase = true).assertIsDisplayed()

        // Click FAB to navigate to Add Ingredient
        composeTestRule.onNodeWithTag("add_ingredient_fab").performClick()

        // Verify we are on Add Ingredient screen by checking the title
        composeTestRule.onNodeWithText("Alta de Stock", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun addIngredient_callsUseCaseAndNavigatesBack() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            StockNavGraph(
                navController = navController,
                stockViewModel = stockViewModel,
                recipeViewModel = recipeViewModel,
                historyViewModel = historyViewModel
            )
            
            // Force navigation to add screen
            LaunchedEffect(Unit) {
                navController.navigate("add_ingredient")
            }
        }

        // Fill form
        composeTestRule.onNodeWithText("Nombre del ingrediente", ignoreCase = true).performTextInput("Azúcar")
        composeTestRule.onNodeWithText("Cantidad", ignoreCase = true).performTextInput("5")
        composeTestRule.onNodeWithText("Precio Total Pagado ($)", ignoreCase = true).performTextInput("250")

        // Click Add button - matching exactly with what is in strings.xml or ignoring case
        composeTestRule.onNodeWithText("Agregar al stock", ignoreCase = true).performClick()

        // Verify Use Case call
        coVerify { 
            addIngredientUseCase(
                name = "Azúcar",
                quantity = 5.0,
                unit = any(), 
                category = any(),
                totalPrice = 250.0
            )
        }
        
        // Verify navigation back (should see inventory title again)
        composeTestRule.onNodeWithText("Inventario", ignoreCase = true).assertIsDisplayed()
    }
}

// Helper because LaunchedEffect is not in scope for the test setup but we can use a Composable
@androidx.compose.runtime.Composable
fun LaunchedEffect(key: Any, block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
    androidx.compose.runtime.LaunchedEffect(key, block)
}
