package com.pentagono.rolematch.domain.usecase

import com.pentagono.rolematch.domain.model.InteractionType
import com.pentagono.rolematch.domain.repository.RestaurantRepository

class SwipeRestaurantUseCase(private val repository: RestaurantRepository) {
    suspend operator fun invoke(restaurantId: String, isLike: Boolean) {
        val interaction = if (isLike) InteractionType.LIKE else InteractionType.DISLIKE
        repository.registerInteraction((restaurantId), interaction)
    }
}