package com.tiendamuna.stock.presentation.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiendamuna.stock.R
import com.tiendamuna.stock.domain.model.Category
import com.tiendamuna.stock.domain.model.MeasureUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    viewModel: StockViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(MeasureUnit.GRAM.symbol) }
    var category by remember { mutableStateOf(Category.OTHERS) }
    var totalPrice by remember { mutableStateOf("") }
    var minThreshold by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.title_add_stock)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            state.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.onEvent(StockEvent.ClearError) }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.ingredient_name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(16.dp))

            CategorySelector(
                selectedCategory = category,
                onCategorySelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = totalPrice,
                onValueChange = { totalPrice = it },
                label = { Text("Precio Total Pagado ($)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = minThreshold,
                onValueChange = { minThreshold = it },
                label = { Text("Alerta Stock Mínimo (Opcional)") },
                placeholder = { Text("Ej: 100") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.onEvent(
                        StockEvent.AddIngredient(
                            name = name, 
                            quantity = quantity.toDoubleOrNull() ?: 0.0, 
                            unit = unit, 
                            category = category,
                            totalPrice = totalPrice.toDoubleOrNull() ?: 0.0,
                            minThreshold = minThreshold.toDoubleOrNull()
                        )
                    )
                    if (state.error == null) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && quantity.isNotBlank() && totalPrice.isNotBlank()
            ) {
                Text(stringResource(R.string.ingredient_add_stock_button))
            }
        }
    }
}
