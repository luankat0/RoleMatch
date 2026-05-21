package com.pentagono.rolematch.data.remote.dto

import com.pentagono.rolematch.data.local.entity.RestaurantEntity
import com.google.gson.annotations.SerializedName

data class RestaurantDto(
    val id: String,
    val nome: String, // FastAPI deve enviar em português como definido no DER
    val endereco: String,
    val telefone: String,
    val descricao: String,
    @SerializedName("image_url") val imageUrl: String,
    val avaliacao: Double
) {
    fun toEntity(): RestaurantEntity {
        return RestaurantEntity(
            id = id,
            name = nome,
            address = endereco,
            phone = telefone,
            description = descricao,
            imageUrl = imageUrl,
            rating = avaliacao
        )
    }
}

data class InteractionRequestDto(
    val restaurant_id: String,
    val tipo: String // "LIKE" | "DISLIKE"
)