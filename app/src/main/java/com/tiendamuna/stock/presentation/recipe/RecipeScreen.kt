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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.domain.model.MeasureUnit
import com.tiendamuna.stock.domain.util.UnitConverter
import com.tiendamuna.stock.presentation.recipe.model.RecipeUiModel
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
        viewModel.loadAvailableIngredients()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Receta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Mis Recetas", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            state.error?.let { error ->
                Text(text = error, color = Color.Red)
                Button(onClick = { viewModel.onEvent(RecipeEvent.ClearError) }) {
                    Text("Limpiar Error")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn {
                items(state.recipes) { recipe ->
                    RecipeItem(
                        recipe = recipe,
                        onPrepare = { 
                            // Map back to domain model for the event
                            val domainRecipe = recipe.toDomain()
                            viewModel.onEvent(RecipeEvent.PrepareRecipe(domainRecipe)) 
                        },
                        onDelete = {
                            viewModel.onEvent(RecipeEvent.DeleteRecipe(recipe.toDomain()))
                        },
                        onEdit = {
                            onNavigateToEdit(recipe.id)
                        }
                    )
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
    var quantity by remember { mutableStateOf("") }
    var selectedIngredient by remember { mutableStateOf<IngredientUiModel?>(null) }
    var selectedUnit by remember { mutableStateOf("") }
    var ingredientExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    val compatibleUnits = remember(selectedIngredient) {
        val ingredientUnit = MeasureUnit.fromSymbol(selectedIngredient?.unit ?: "")
        MeasureUnit.entries.filter { it.type == ingredientUnit.type }.map { it.symbol }
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
                Text(text = "Seleccionar Ingrediente", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    OutlinedTextField(
                        value = selectedIngredient?.name ?: "Elegir del stock",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { ingredientExpanded = true },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientExpanded) },
                        label = { Text("Ingrediente") }
                    )
                    DropdownMenu(expanded = ingredientExpanded, onDismissRequest = { ingredientExpanded = false }) {
                        available.forEach { ingredient ->
                            DropdownMenuItem(
                                text = { Text("${ingredient.name} (${ingredient.quantityDisplay})") },
                                onClick = {
                                    selectedIngredient = ingredient
                                    selectedUnit = ingredient.unit
                                    ingredientExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f),
                        isError = quantity.isNotEmpty() && (quantity.toDoubleOrNull() ?: 0.0) <= 0.0
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(0.6f)) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            modifier = Modifier.fillMaxWidth().clickable { unitExpanded = true },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }
                        )
                        DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            compatibleUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
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
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
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
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = recipe.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            recipe.ingredients.forEach { 
                Text(text = "• ${it.name}: ${it.quantityDisplay}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPrepare, modifier = Modifier.align(Alignment.End)) {
                Text("Preparar (Restar Stock)")
            }
        }
    }
}
