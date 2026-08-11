package com.tiendamuna.stock.presentation.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tiendamuna.stock.R
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.model.MeasureUnit
import com.tiendamuna.stock.presentation.common.components.DropDownList
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import com.tiendamuna.stock.presentation.stock.model.StockStatus
import java.util.Locale

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
    var ingredientToDelete by remember { mutableStateOf<IngredientUiModel?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title_inventory),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Summary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = stringResource(R.string.label_total_ingredients),
                    value = state.totalIngredientsCount.toString(),
                    subtitle = stringResource(R.string.subtitle_in_catalog),
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(R.string.label_total_valuation),
                    value = state.totalValuationDisplay,
                    subtitle = stringResource(R.string.subtitle_capital_invested),
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1.3f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                SummaryCard(
                    title = stringResource(R.string.label_alerts),
                    value = state.lowStockCount.toString(),
                    subtitle = stringResource(R.string.subtitle_critical_stock),
                    icon = Icons.Default.ReportProblem,
                    modifier = Modifier.weight(1f),
                    valueColor = if (state.lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(StockEvent.SearchQueryChanged(it)) },
                placeholder = { Text(stringResource(R.string.ingredient_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Simplified Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.header_ingredient), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(2f))
                Text(stringResource(R.string.header_stock), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                Text(stringResource(R.string.header_value), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(48.dp)) // Menu icon space
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = state.ingredients,
                    key = { it.id }
                ) { ingredient ->
                    CompactStockItem(
                        ingredient = ingredient,
                        onEdit = { ingredientToEdit = ingredient },
                        onDelete = { ingredientToDelete = ingredient }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddIngredient,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_ingredient_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
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

    if (ingredientToDelete != null) {
        AlertDialog(
            onDismissRequest = { ingredientToDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_ingredient_title)) },
            text = { Text(stringResource(R.string.dialog_delete_ingredient_message, ingredientToDelete?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingredientToDelete?.let {
                            viewModel.onEvent(
                                StockEvent.DeleteIngredient(
                                    Ingredient(
                                        id = it.id,
                                        name = it.name,
                                        quantity = it.rawQuantity,
                                        unit = it.unit
                                    )
                                )
                            )
                        }
                        ingredientToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredientToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontSize = 8.sp)
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(12.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = valueColor, fontSize = 15.sp, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontSize = 8.sp)
        }
    }
}

@Composable
fun CompactStockItem(
    ingredient: IngredientUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Initial Avatar (Smaller)
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = ingredient.name.take(1).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Name & Threshold
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (ingredient.minThreshold != null) {
                Text(
                    text = stringResource(R.string.label_min_threshold, String.format(Locale.getDefault(), "%.2f", ingredient.minThreshold), ingredient.unit),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp
                )
            }
        }

        // Current Stock
        Text(
            text = ingredient.quantityDisplay,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = when(ingredient.status) {
                StockStatus.OUT_OF_STOCK -> MaterialTheme.colorScheme.error
                StockStatus.LOW_STOCK -> MaterialTheme.colorScheme.primary
                else -> Color.White
            }
        )

        // Valuation
        Text(
            text = ingredient.valuationDisplay,
            modifier = Modifier.weight(1.4f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )

        // Three dots Menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.options),
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit)) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                )
            }
        }
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
                    text = stringResource(R.string.title_edit_ingredient),
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
                    label = { Text(stringResource(R.string.label_price_per_unit_field)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = minThreshold,
                    onValueChange = { minThreshold = it },
                    label = { Text(stringResource(R.string.label_threshold_optional)) },
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
