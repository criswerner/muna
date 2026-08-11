package com.tiendamuna.stock.presentation.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.model.MeasureUnit
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import com.tiendamuna.stock.presentation.stock.model.StockStatus
import com.tiendamuna.stock.R
import com.tiendamuna.stock.presentation.common.components.DropDownList

@Composable
fun UnitSelector(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val units = MeasureUnit.getAllSymbols()
    DropDownList(
        label = stringResource(R.string.unit_field),
        selectedItem = selectedUnit,
        items = units,
        itemToString = { it },
        onItemSelected = onUnitSelected,
        modifier = modifier
    )
}

@Composable
fun CategorySelector(
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    DropDownList(
        label = stringResource(R.string.category_field),
        selectedItem = selectedCategory,
        items = Category.entries,
        itemToString = { it.displayName },
        onItemSelected = onCategorySelected,
        modifier = modifier
    )
}

@Composable
fun StockScreen(
    viewModel: StockViewModel,
    onNavigateToAddIngredient: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var ingredientToEdit by remember { mutableStateOf<IngredientUiModel?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_inventory),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(StockEvent.SearchQueryChanged(it)) },
                placeholder = { Text(stringResource(R.string.ingredient_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.ingredients) { ingredient ->
                    StockItem(
                        ingredient = ingredient,
                        onDelete = {
                            viewModel.onEvent(
                                StockEvent.DeleteIngredient(
                                    Ingredient(
                                        id = ingredient.id,
                                        name = ingredient.name,
                                        quantity = ingredient.rawQuantity,
                                        unit = ingredient.unit,
                                        minThreshold = ingredient.minThreshold
                                    )
                                )
                            )
                        },
                        onEdit = { ingredientToEdit = ingredient }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddIngredient,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_ingredient_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.title_add_stock))
        }
    }

    if (ingredientToEdit != null) {
        EditIngredientDialog(
            ingredient = ingredientToEdit!!,
            onDismiss = { ingredientToEdit = null },
            onConfirm = { updated ->
                viewModel.onEvent(StockEvent.UpdateIngredient(updated))
                ingredientToEdit = null
            }
        )
    }
}

@Composable
fun EditIngredientDialog(
    ingredient: IngredientUiModel,
    onDismiss: () -> Unit,
    onConfirm: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf(ingredient.name) }
    var quantity by remember { mutableStateOf(ingredient.rawQuantity.toString()) }
    var unit by remember { mutableStateOf(ingredient.unit) }
    var pricePerUnit by remember { mutableStateOf(ingredient.pricePerUnit.toString()) }
    var minThreshold by remember { mutableStateOf(ingredient.minThreshold?.toString() ?: "") }
    var category by remember {
        mutableStateOf(Category.entries.find { it.displayName == ingredient.categoryName } ?: Category.OTHERS)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.title_edit_recipes),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_field)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.quantity_field)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                UnitSelector(
                    selectedUnit = unit,
                    onUnitSelected = { unit = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategorySelector(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pricePerUnit,
                    onValueChange = { pricePerUnit = it },
                    label = { Text("Precio por unidad ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = minThreshold,
                    onValueChange = { minThreshold = it },
                    label = { Text("Alerta Stock Mínimo (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { 
                        Text(stringResource(R.string.cancel)) 
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                Ingredient(
                                    id = ingredient.id,
                                    name = name,
                                    quantity = quantity.toDoubleOrNull() ?: 0.0,
                                    unit = unit,
                                    category = category,
                                    pricePerUnit = pricePerUnit.toDoubleOrNull() ?: 0.0,
                                    minThreshold = minThreshold.toDoubleOrNull()
                                )
                            )
                        },
                        enabled = name.isNotBlank() && quantity.toDoubleOrNull() != null && pricePerUnit.toDoubleOrNull() != null
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun StockItem(
    ingredient: IngredientUiModel,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val cardColor = when (ingredient.status) {
        StockStatus.OUT_OF_STOCK -> MaterialTheme.colorScheme.errorContainer
        StockStatus.LOW_STOCK -> Color(0xFFFFF3E0) // Light orange
        StockStatus.NORMAL -> MaterialTheme.colorScheme.surface
    }

    val icon = when (ingredient.status) {
        StockStatus.OUT_OF_STOCK -> Icons.Default.Error
        StockStatus.LOW_STOCK -> Icons.Default.Warning
        StockStatus.NORMAL -> null
    }

    val statusTint = when (ingredient.status) {
        StockStatus.OUT_OF_STOCK -> MaterialTheme.colorScheme.error
        StockStatus.LOW_STOCK -> Color(0xFFE65100) // Dark orange
        StockStatus.NORMAL -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.padding(end = 12.dp).size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = ingredient.name, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (ingredient.status == StockStatus.OUT_OF_STOCK) MaterialTheme.colorScheme.onErrorContainer else Color.Unspecified
                    )
                    Text(
                        text = "${ingredient.categoryName} • ${ingredient.quantityDisplay} • ${ingredient.priceDisplay}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (ingredient.status == StockStatus.OUT_OF_STOCK) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary
                    )
                    if (ingredient.status == StockStatus.LOW_STOCK) {
                        Text(
                            text = "Stock bajo (Mín: ${ingredient.minThreshold} ${ingredient.unit})",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusTint
                        )
                    }
                }
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
    }
}
