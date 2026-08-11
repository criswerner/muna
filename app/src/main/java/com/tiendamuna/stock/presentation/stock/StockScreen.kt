package com.tiendamuna.stock.presentation.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.model.MeasureUnit
import com.tiendamuna.stock.presentation.stock.model.IngredientUiModel
import com.tiendamuna.stock.presentation.stock.model.StockStatus
import com.tiendamuna.stock.R
import com.tiendamuna.stock.presentation.common.components.DropDownList
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
                    title = "TOTAL INGREDIENTES",
                    value = state.totalIngredientsCount.toString(),
                    subtitle = "En catálogo",
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "VALUACIÓN TOTAL",
                    value = state.totalValuationDisplay,
                    subtitle = "Capital invertido",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1.3f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                SummaryCard(
                    title = "ALERTAS",
                    value = state.lowStockCount.toString(),
                    subtitle = "Stock crítico",
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
                Text("INGREDIENTE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(2f))
                Text("STOCK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                Text("VALOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(64.dp)) // Space for actions
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                items(state.ingredients) { ingredient ->
                    CompactStockItem(
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
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
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
                    text = "Mín: ${ingredient.minThreshold} ${ingredient.unit}",
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
                StockStatus.LOW_STOCK -> MaterialTheme.colorScheme.primary // Amber
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
            color = MaterialTheme.colorScheme.tertiary // Emerald
        )

        // Actions
        Row(
            modifier = Modifier.width(64.dp), 
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
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
                    text = "Editar Ingrediente",
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
