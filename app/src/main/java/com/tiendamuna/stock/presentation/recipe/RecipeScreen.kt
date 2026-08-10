package com.tiendamuna.stock.presentation.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
            Text(text = stringResource(R.string.title_recipes), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            state.error?.let { error ->
                Text(text = error, color = Color.Red)
                Button(onClick = { viewModel.onEvent(RecipeEvent.ClearError) }) {
                    Text("OK")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva Receta")
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
                Text(text = "Preparar ${recipe.name}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Cuántos lotes deseas preparar?", 
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total a producir: ${(batches.toDoubleOrNull() ?: 0.0) * recipe.yieldQuantity} ${recipe.yieldUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = batches,
                    onValueChange = { batches = it },
                    label = { Text("Cantidad de lotes") },
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
                        enabled = batches.toDoubleOrNull() != null && batches.toDouble() > 0
                    ) {
                        Text("Confirmar")
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Seleccionar Ingrediente", style = MaterialTheme.typography.titleMedium) // Could use resource
                Spacer(modifier = Modifier.height(16.dp))

                DropDownList(
                    label = stringResource(R.string.name_field),
                    selectedItem = selectedIngredient,
                    items = available,
                    itemToString = { it?.let { "${it.name} (${it.quantityDisplay})" } ?: "Elegir del stock" },
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
                            text = "Aviso: Supera el stock actual ($availableQty ${selectedIngredient!!.unit})",
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
                        Text("Añadir")
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(
    recipe: RecipeUiModel, 
    onPrepare: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = recipe.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Rinde: ${recipe.yieldDisplay} • Costo: ${recipe.costDisplay} (${recipe.costPerYieldUnitDisplay})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            recipe.ingredients.forEach { 
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "• ${it.name}: ${it.quantityDisplay}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = it.costDisplay, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPrepare, modifier = Modifier.align(Alignment.End)) {
                Text("Preparar (Restar Stock)")
            }
        }
    }
}
