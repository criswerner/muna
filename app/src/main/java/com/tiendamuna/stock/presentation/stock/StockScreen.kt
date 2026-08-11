package com.tiendamuna.stock.presentation.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
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
    val statusColor = when (ingredient.status) {
        StockStatus.OUT_OF_STOCK -> MaterialTheme.colorScheme.error
        StockStatus.LOW_STOCK -> Color(0xFFF57C00) // Vibrant orange
        StockStatus.NORMAL -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Lateral status indicator
            if (ingredient.status != StockStatus.NORMAL) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(statusColor)
                )
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // 2. Category tag
                    Text(
                        text = ingredient.categoryName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // 3. Ingredient name
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. Status Badge (Chip)
                    if (ingredient.status != StockStatus.NORMAL) {
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (ingredient.status == StockStatus.OUT_OF_STOCK) 
                                        Icons.Default.Error else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (ingredient.status == StockStatus.OUT_OF_STOCK) 
                                        "Sin Stock" else "Stock Bajo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 5. Quantity and Price info
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = ingredient.quantityDisplay,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (ingredient.status == StockStatus.OUT_OF_STOCK) 
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = ingredient.priceDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // 6. Minimalist action buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
