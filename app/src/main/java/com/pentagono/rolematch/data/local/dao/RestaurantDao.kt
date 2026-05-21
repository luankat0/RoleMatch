package com.pentagono.rolematch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pentagono.rolematch.data.local.entity.RestaurantEntity

@Dao
interface RestaurantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restaurants: List<RestaurantEntity>)

    // Pega o primeiro da fila
    @Query("SELECT * FROM restaurant_queue LIMIT 1")
    suspend fun getNextRestaurant(): RestaurantEntity?

    // Remove o restaurante após o usuário dar Swipe
    @Query("DELETE FROM restaurant_queue WHERE id = :restaurantId")
    suspend fun removeRestaurantFromQueue(restaurantId: String)

    // Limpa a fila antiga quando uma nova busca é feita
    @Query("DELETE FROM restaurant_queue")
    suspend fun clearQueue()
}