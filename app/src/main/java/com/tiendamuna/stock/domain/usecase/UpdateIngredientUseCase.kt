package com.tiendamuna.stock.domain.usecase

import com.tiendamuna.stock.domain.model.DomainException
import com.tiendamuna.stock.domain.model.Ingredient
import com.tiendamuna.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.first

class UpdateIngredientUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(ingredient: Ingredient) {
        if (ingredient.name.isBlank()) throw DomainException.EmptyName
        if (ingredient.quantity < 0) throw DomainException.InvalidQuantity
        if (ingredient.pricePerUnit < 0) throw DomainException.InvalidQuantity // Reuse or specific one
        
        val currentStock = repository.getStock().first()
        val nameAlreadyTaken = currentStock.any { 
            it.id != ingredient.id && it.name.trim().lowercase() == ingredient.name.trim().lowercase() 
        }
        
        if (nameAlreadyTaken) {
            throw DomainException.NameAlreadyTaken(ingredient.name)
        }

        repository.updateIngredient(ingredient)
    }
}
