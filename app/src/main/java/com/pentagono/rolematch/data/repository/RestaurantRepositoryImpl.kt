package com.pentagono.rolematch.data.repository

import com.pentagono.rolematch.data.local.dao.RestaurantDao
import com.pentagono.rolematch.data.remote.RoleMatchApi
import com.pentagono.rolematch.data.remote.dto.InteractionRequestDto
import com.pentagono.rolematch.domain.model.InteractionType
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.repository.RestaurantRepository

class RestaurantRepositoryImpl(
    private val api: RoleMatchApi,
    private val dao: RestaurantDao
) : RestaurantRepository {

    override suspend fun fetchAndCacheRestaurantQueue(lat: Double, lng: Double, radiusKm: Int) {
        try {
            // Requisita lote de restaurantes da API
            val response = api.getRestaurantQueue(lat, lng, radiusKm)

            if (response.isSuccessful && response.body() != null) {
                // Mapeia DTOs recebidos para Entidades do SQLite
                val entities = response.body()!!.map { it.toEntity() }

                dao.clearQueue()
                dao.insertAll(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getNextRestaurantFromCache(): Restaurant? {
        val entity = dao.getNextRestaurant()
        return entity?.toDomainModel()
    }

    override suspend fun registerInteraction(restaurantId: String, interaction: InteractionType) {
        // Remove da fila local
        dao.removeRestaurantFromQueue(restaurantId)

        // Envia a decisão para o backend alimentar o motor de IA
        try {
            val requestDto = InteractionRequestDto(
                restaurant_id = restaurantId,
                tipo = interaction.name
            )
            api.sendInteraction(requestDto)
        } catch (e: Exception) {
            // Lógica de retry caso de falha durante o swipe
        }
    }
}