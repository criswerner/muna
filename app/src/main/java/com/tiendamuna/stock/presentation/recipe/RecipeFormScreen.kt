package com.tiendamuna.stock.presentation.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tiendamuna.stock.domain.model.Recipe
import com.tiendamuna.stock.domain.model.RecipeIngredient
import com.tiendamuna.stock.presentation.recipe.model.RecipeUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormScreen(
    viewModel: RecipeViewModel,
    recipeId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Find initial recipe if editing
    val initialRecipe = remember(recipeId, state.recipes) {
        state.recipes.find { it.id == recipeId }
    }

    var name by remember { mutableStateOf(initialRecipe?.name ?: "") }
    var selectedIngredients by remember { 
        mutableStateOf(
            initialRecipe?.ingredients?.map { 
                RecipeIngredient(it.ingredientId, it.name, it.rawQuantity, it.unit) 
            } ?: emptyList<RecipeIngredient>()
        ) 
    }
    
    var showIngredientPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipeId == null) "Nueva Receta" else "Editar Receta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val recipe = Recipe(
                                id = recipeId ?: java.util.UUID.randomUUID().toString(),
                                name = name,
                                ingredients = selectedIngredients
                            )
                            if (recipeId == null) {
                                viewModel.onEvent(RecipeEvent.AddRecipe(recipe))
                            } else {
                                viewModel.onEvent(RecipeEvent.UpdateRecipe(recipe))
                            }
                            onNavigateBack()
                        },
                        enabled = name.isNotBlank() && selectedIngredients.isNotEmpty()
                    ) {
                        Text("GUARDAR", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la receta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ingredientes", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { showIngredientPicker = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (selectedIngredients.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay ingredientes añadidos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(selectedIngredients) { ingredient ->
                        IngredientRow(
                            ingredient = ingredient,
                            onRemove = {
                                selectedIngredients = selectedIngredients.filter { it.ingredientId != ingredient.ingredientId }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showIngredientPicker) {
        IngredientPicker(
            available = state.availableIngredients,
            onDismiss = { showIngredientPicker = false },
            onSelected = { recipeIngredient ->
                // Check if already added
                if (selectedIngredients.any { it.ingredientId == recipeIngredient.ingredientId }) {
                    selectedIngredients = selectedIngredients.map {
                        if (it.ingredientId == recipeIngredient.ingredientId) recipeIngredient else it
                    }
                } else {
                    selectedIngredients = selectedIngredients + recipeIngredient
                }
                showIngredientPicker = false
            }
        )
    }
}

@Composable
fun IngredientRow(
    ingredient: RecipeIngredient,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${ingredient.quantityRequired} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
