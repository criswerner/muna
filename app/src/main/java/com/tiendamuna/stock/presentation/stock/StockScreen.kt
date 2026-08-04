package com.tiendamuna.stock.presentation.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.model.MeasureUnit
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
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
    viewModel: StockViewModel
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(MeasureUnit.GRAM.symbol) }
    var category by remember { mutableStateOf(Category.OTHERS) }
    var totalPrice by remember { mutableStateOf("") }

    var ingredientToEdit by remember { mutableStateOf<IngredientUiModel?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = stringResource(R.string.title_add_stock), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        state.error?.let { error ->
            Text(text = error, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            Button(onClick = { viewModel.onEvent(StockEvent.ClearError) }) {
                Text("OK") // Simple enough or could add R.string.ok
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.ingredient_name_hint)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text(stringResource(R.string.quantity_field)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            UnitSelector(
                selectedUnit = unit,
                onUnitSelected = { unit = it },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        CategorySelector(
            selectedCategory = category,
            onCategorySelected = { category = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = totalPrice,
            onValueChange = { totalPrice = it },
            label = { Text("Precio Total Pagado ($)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.onEvent(
                    StockEvent.AddIngredient(
                        name, 
                        quantity.toDoubleOrNull() ?: 0.0, 
                        unit, 
                        category,
                        totalPrice.toDoubleOrNull() ?: 0.0
                    )
                )
                name = ""
                quantity = ""
                unit = MeasureUnit.GRAM.symbol
                category = Category.OTHERS
                totalPrice = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.ingredient_add_stock_button))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_inventory),
                style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(StockEvent.SearchQueryChanged(it)) },
                placeholder = { Text(stringResource(R.string.ingredient_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1.5f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
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
                                    unit = ingredient.unit
                                )
                            )
                        )
                    },
                    onEdit = { ingredientToEdit = ingredient }
                )
            }
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
    var category by remember {
        mutableStateOf(Category.entries.find { it.displayName == ingredient.categoryName } ?: Category.OTHERS)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                    pricePerUnit = pricePerUnit.toDoubleOrNull() ?: 0.0
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${ingredient.categoryName} • ${ingredient.quantityDisplay} • ${ingredient.priceDisplay}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
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
    }
}
