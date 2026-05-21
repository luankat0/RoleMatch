package com.pentagono.rolematch.domain.repository

import com.pentagono.rolematch.domain.model.InteractionType
import com.pentagono.rolematch.domain.model.Restaurant

interface RestaurantRepository {
    // Busca na API os top 20 e salva no cache local
    suspend fun fetchAndCacheRestaurantQueue(lat: Double, lng: Double, radiusKm: Int)

    // Pega o próximo cache para exibir na tela
    suspend fun getNextRestaurantFromCache(): Restaurant?

    // Registra o swipe (curtir/rejeitar) e envia para a API
    suspend fun registerInteraction(restaurantId: String, interaction: InteractionType)
}