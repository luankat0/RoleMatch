package com.pentagono.rolematch.data.remote

import com.pentagono.rolematch.data.remote.dto.RestaurantDto
import com.pentagono.rolematch.data.remote.dto.InteractionRequestDto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface RoleMatchApi {
    
    @GET("restaurantes/fila")
    suspend fun getRestaurantQueue(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("raio") radius: Int
    ): Response<List<RestaurantDto>>

    @POST("interacoes")
    suspend fun sendInteraction(
        @Body request: InteractionRequestDto
    ): Response<Unit>
}