package com.pentagono.rolematch.data.local.entity

import androidx.room.entity
import androidx.room.PrimaryKey
import com.pentagono.rolematch.domain.model.Restaurant

@Entity(tableName = "restaurant_queue")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val description: String,
    val imageUrl: String,
    val rating: Double
) {
    fun toDomainModel(): Restaurant {
        return Restaurant(id, name, address, phone, description, imageUrl, rating)
    }
}