package com.pentagono.rolematch.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface RoleMatchApi {
    
    // Rota que busca os restaurantes compatíveis com o usuário
    @GET("/api/v1/match")
    suspend fun getRecommendations(
        @Header("Authorization") token: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius_km") radius: Int
    ): Response<List<RestaurantResponse>> 
}