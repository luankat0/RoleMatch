package com.pentagono.rolematch.domain.usecase

import com.pentagono.rolematch.domain.repository.RestaurantRepository

class FetchRestaurantUseCase(private val repository: RestaurantRepository) {
    suspend operator fun invoke(lat: Double, lng: Double, radiusKm: Int) {
        repository.fetchAndCacheRestaurantQueue(lat, lng, radiusKm)
    }
}