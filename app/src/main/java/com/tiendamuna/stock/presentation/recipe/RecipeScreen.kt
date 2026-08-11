package com.tiendamuna.stock.presentation.recipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tiendamuna.stock.R
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.domain.model.MeasureUnit
import com.tiendamuna.stock.domain.util.UnitConverter
import com.tiendamuna.stock.presentation.recipe.model.RecipeUiModel
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import com.tiendamuna.stock.utils.empty
import com.tiendamuna.stock.presentation.common.components.DropDownList
import java.util.Locale

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var recipeToPrepare by remember { mutableStateOf<RecipeUiModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
        viewModel.loadAvailableIngredients()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title_recipes), 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            state.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.onEvent(RecipeEvent.ClearError) }) {
                    Text(stringResource(R.string.ok))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.recipes) { recipe ->
                    RecipeItem(
                        recipe = recipe,
                        onPrepare = { 
                            recipeToPrepare = recipe
                        },
                        onDelete = {
                            viewModel.onEvent(RecipeEvent.DeleteRecipe(recipe.toDomain()))
                        },
                        onEdit = {
                            onNavigateToEdit(recipe.id)
                        },
                        onClick = {
                            onNavigateToDetail(recipe.id)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.title_new_recipe))
        }
    }

    if (recipeToPrepare != null) {
        PrepareRecipeDialog(
            recipe = recipeToPrepare!!,
            onDismiss = { recipeToPrepare = null },
            onConfirm = { batches ->
                viewModel.onEvent(RecipeEvent.PrepareRecipe(recipeToPrepare!!.toDomain(), batches))
                recipeToPrepare = null
            }
        )
    }
}

@Composable
fun PrepareRecipeDialog(
    recipe: RecipeUiModel,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var batches by remember { mutableStateOf("1") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.dialog_prepare_title, recipe.name), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dialog_prepare_question), 
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.dialog_prepare_total, String.format(Locale.getDefault(), "%.2f", (batches.toDoubleOrNull() ?: 0.0) * recipe.yieldQuantity), recipe.yieldUnit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = batches,
                    onValueChange = { batches = it },
                    label = { Text(stringResource(R.string.label_batches_quantity)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = batches.isNotEmpty() && (batches.toDoubleOrNull() ?: 0.0) <= 0.0
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(batches.toDoubleOrNull() ?: 1.0)
                        },
                        enabled = batches.toDoubleOrNull() != null && (batches.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text(stringResource(R.string.button_confirm))
                    }
                }
            }
        }
    }
}

// Helper to avoid repetition in mapping back
fun RecipeUiModel.toDomain() = Recipe(
    id = id,
    name = name,
    ingredients = ingredients.map { 
        RecipeIngredient(
            ingredientId = it.ingredientId,
            name = it.name,
            quantityRequired = it.rawQuantity,
            unit = it.unit
        )
    },
    instructions = instructions
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientPicker(
    available: List<IngredientUiModel>,
    onDismiss: () -> Unit,
    onSelected: (RecipeIngredient) -> Unit
) {
    var quantity by remember { mutableStateOf(String.empty()) }
    var selectedIngredient by remember { mutableStateOf<IngredientUiModel?>(null) }
    var selectedUnit by remember { mutableStateOf(String.empty()) }

    val compatibleUnits = remember(selectedIngredient) {
        MeasureUnit.getCompatibleUnits(selectedIngredient?.unit ?: String.empty())
    }

    LaunchedEffect(selectedIngredient) {
        if (selectedIngredient != null && selectedUnit.isEmpty()) {
            selectedUnit = selectedIngredient!!.unit
        }
    }

    val placeholderChoose = stringResource(R.string.placeholder_choose_from_stock)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.dialog_select_ingredient_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                DropDownList(
                    label = stringResource(R.string.name_field),
                    selectedItem = selectedIngredient,
                    items = available,
                    itemToString = { it?.let { "${it.name} (${it.quantityDisplay})" } ?: placeholderChoose },
                    onItemSelected = { 
                        selectedIngredient = it
                        selectedUnit = it?.unit ?: String.empty()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(stringResource(R.string.quantity_field)) },
                        modifier = Modifier.weight(1f),
                        isError = quantity.isNotEmpty() && (quantity.toDoubleOrNull() ?: 0.0) <= 0.0
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    DropDownList(
                        label = stringResource(R.string.unit_field),
                        selectedItem = selectedUnit,
                        items = compatibleUnits,
                        itemToString = { it },
                        onItemSelected = { selectedUnit = it },
                        modifier = Modifier.weight(0.6f)
                    )
                }

                if (selectedIngredient != null && quantity.isNotEmpty()) {
                    val neededRaw = quantity.toDoubleOrNull() ?: 0.0
                    val availableQty = selectedIngredient!!.rawQuantity
                    
                    val neededInStockUnit = UnitConverter.convert(
                        amount = neededRaw,
                        fromUnitSymbol = selectedUnit,
                        toUnitSymbol = selectedIngredient!!.unit
                    )
                    
                    if (neededInStockUnit > availableQty) {
                        Text(
                            text = stringResource(R.string.warning_exceeds_stock, String.format(Locale.getDefault(), "%.2f", availableQty), selectedIngredient!!.unit),
                            color = Color(0xFFFFA500),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = {
                            selectedIngredient?.let {
                                onSelected(
                                    RecipeIngredient(
                                        ingredientId = it.id,
                                        name = it.name,
                                        quantityRequired = quantity.toDoubleOrNull() ?: 0.0,
                                        unit = selectedUnit
                                    )
                                )
                            }
                        },
                        enabled = selectedIngredient != null && (quantity.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text(stringResource(R.string.button_add))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeItem(
    recipe: RecipeUiModel, 
    onPrepare: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    var showInstructions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recipe Icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.label_yield_batch, recipe.yieldDisplay),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Status Badge
                Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.status_ready),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Cost Summary Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_cost_batch),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = recipe.costDisplay,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.label_cost_unit),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = recipe.costPerYieldUnitDisplay,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Ingredients Section
            Text(
                text = stringResource(R.string.label_ingredients_count, recipe.ingredients.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recipe.ingredients.forEach { ingredient ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "${ingredient.name}: ${ingredient.quantityDisplay}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Instructions Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInstructions = !showInstructions },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.text_view_instructions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (showInstructions) {
                Text(
                    text = if (recipe.instructions.isBlank()) stringResource(R.string.text_no_instructions) else recipe.instructions,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                Button(
                    onClick = onPrepare,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.button_prepare_batch),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
