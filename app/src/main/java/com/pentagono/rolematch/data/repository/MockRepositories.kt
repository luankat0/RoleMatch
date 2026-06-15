package com.pentagono.rolematch.data.repository

import com.pentagono.rolematch.domain.model.*
import com.pentagono.rolematch.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockFavoriteRepository : FavoriteRepository {
    private val favorites = mutableSetOf<String>()

    override suspend fun getUserFavorites(userId: String): Result<List<String>> =
        Result.success(favorites.toList())

    override suspend fun addFavorite(userId: String, restaurantId: String): Result<Unit> {
        favorites.add(restaurantId)
        return Result.success(Unit)
    }

    override suspend fun removeFavorite(userId: String, restaurantId: String): Result<Unit> {
        favorites.remove(restaurantId)
        return Result.success(Unit)
    }

    override suspend fun isFavorite(userId: String, restaurantId: String): Result<Boolean> =
        Result.success(restaurantId in favorites)

    override fun observeFavorites(userId: String): Flow<List<String>> =
        flow { emit(favorites.toList()) }
}

class MockInteractionRepository : InteractionRepository {
    override suspend fun logInteraction(
        userId: String,
        restaurantId: String,
        type: InteractionType,
        metadata: Map<String, String>
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getUserInteractions(userId: String): Result<List<Map<String, Any>>> =
        Result.success(emptyList())
}