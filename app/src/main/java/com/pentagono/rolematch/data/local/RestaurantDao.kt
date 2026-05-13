package com.pentagono.rolematch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

interface RestaurantDao {

    // Salva o lote de restaurantes recomendados no cache local
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchQueue(restaurants: List<RestaurantEntity>)

    // Busca a fila salva para o usuário interagir via swipe
    @Query("SELECT * FROM restaurants")
    suspend fun getCachedQueue(): List<RestaurantEntity>

    @query("DELETE FROM restaurants")
    suspend fun clearQueue()
}