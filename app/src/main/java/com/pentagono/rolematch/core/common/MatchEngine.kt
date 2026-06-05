package com.pentagono.rolematch.core.common

import com.pentagono.rolematch.domain.model.*
import kotlin.math.*

/**
 * MatchEngine – scores restaurants against a user's MatchPreferences.
 *
 * Scoring rubric:
 *  +30 per cuisine type that matches
 *  +20 per establishment type that matches
 *  +15 if price range matches exactly, +7 if ±1
 *  +15 if within max distance (scaled by proximity)
 *  +10 for each desired special feature present
 *  +10 per past LIKE/FAVORITE on a similar restaurant
 *  −30 per past DISLIKE on a similar restaurant
 *  +4 per rating star (max 20)
 */
object MatchEngine {

    fun score(
        restaurant: Restaurant,
        preferences: MatchPreferences,
        userInteractions: List<Pair<InteractionType, Set<String>>> = emptyList()
    ): MatchResult {
        var score = 0
        val reasons = mutableListOf<String>()

        // ── Cuisine ──────────────────────────────────────────────────────────
        val cuisineHits = restaurant.cuisineTypes.intersect(preferences.cuisineTypes.toSet()).size
        if (cuisineHits > 0) {
            score += cuisineHits * 30
            reasons += "Culinária compatível"
        }

        // ── Establishment type ────────────────────────────────────────────────
        val typeHits = restaurant.establishmentTypes.intersect(preferences.establishmentTypes.toSet()).size
        if (typeHits > 0) {
            score += typeHits * 20
            reasons += "Tipo de local ideal"
        }

        // ── Price range ───────────────────────────────────────────────────────
        preferences.priceRange?.let { pref ->
            val diff = abs(restaurant.priceRange - pref)
            when (diff) {
                0 -> { score += 15; reasons += "Faixa de preço perfeita" }
                1 -> score += 7
            }
        }

        // ── Distance ──────────────────────────────────────────────────────────
        if (restaurant.distanceKm <= preferences.maxDistanceKm) {
            val proximity = 1f - (restaurant.distanceKm / preferences.maxDistanceKm)
            score += (proximity * 15).toInt()
            if (restaurant.distanceKm <= 1f) reasons += "Muito perto de você"
            else if (restaurant.distanceKm <= 2f) reasons += "Perto de você"
        } else {
            score -= 20 // too far
        }

        // ── Special features ──────────────────────────────────────────────────
        if (preferences.petFriendly && restaurant.features.petFriendly) { score += 10; reasons += "Pet Friendly ✓" }
        if (preferences.kidsArea && restaurant.features.kidsArea) { score += 10; reasons += "Área Kids ✓" }
        if (preferences.accessiblePcd && restaurant.features.accessiblePcd) { score += 10; reasons += "Acessível PcD ✓" }

        // ── Dietary restrictions (no penalty for missing info in mock) ────────
        // In production: check restaurant.dietaryOptions against preferences.dietaryRestrictions

        // ── Rating bonus ──────────────────────────────────────────────────────
        score += (restaurant.rating * 4).toInt().coerceAtMost(20)
        if (restaurant.rating >= 4.5f) reasons += "Muito bem avaliado ⭐"

        // ── Past interactions (simple boost/penalty) ──────────────────────────
        userInteractions.forEach { (type, restaurantIds) ->
            if (restaurant.id in restaurantIds) {
                when (type) {
                    InteractionType.LIKE, InteractionType.FAVORITE -> { score += 10; reasons += "Você gostou de lugares similares" }
                    InteractionType.DISLIKE -> score -= 30
                    else -> Unit
                }
            }
        }

        return MatchResult(
            restaurant = restaurant,
            score = score.coerceAtLeast(0),
            matchReasons = reasons.distinct()
        )
    }

    fun rank(
        restaurants: List<Restaurant>,
        preferences: MatchPreferences,
        userInteractions: List<Pair<InteractionType, Set<String>>> = emptyList()
    ): List<MatchResult> = restaurants
        .map { score(it, preferences, userInteractions) }
        .filter { it.score > 0 }
        .sortedByDescending { it.score }
}
