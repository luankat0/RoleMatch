package com.pentagono.rolematch.presentation.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.domain.model.MatchResult
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.model.RestaurantStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.pentagono.rolematch.core.common.MockData

data class MatchState(
    val selectedCuisines: Set<String> = emptySet(),
    val selectedTypes: Set<String> = emptySet(),
    val selectedRestrictions: Set<String> = emptySet(),
    val priceRange: Int? = null,
    val maxDistance: Float = 5f,
    val petFriendly: Boolean = false,
    val kidsArea: Boolean = false,
    val accessiblePcd: Boolean = false,
    val results: List<MatchResult> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)

class MatchViewModel : ViewModel() {

    private val _state = MutableStateFlow(MatchState())
    val state: StateFlow<MatchState> = _state.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    fun onCuisineToggle(item: String) {
        val current = _state.value.selectedCuisines
        _state.value = _state.value.copy(
            selectedCuisines = if (item in current) current - item else current + item
        )
    }

    fun onTypeToggle(item: String) {
        val current = _state.value.selectedTypes
        _state.value = _state.value.copy(
            selectedTypes = if (item in current) current - item else current + item
        )
    }

    fun onRestrictionToggle(item: String) {
        val current = _state.value.selectedRestrictions
        _state.value = _state.value.copy(
            selectedRestrictions = if (item in current) current - item else current + item
        )
    }

    fun onPriceRangeChange(value: Int?) {
        _state.value = _state.value.copy(priceRange = value)
    }

    fun onMaxDistanceChange(value: Float) {
        _state.value = _state.value.copy(maxDistance = value)
    }

    fun onPetFriendlyToggle() {
        _state.value = _state.value.copy(petFriendly = !_state.value.petFriendly)
    }

    fun onKidsAreaToggle() {
        _state.value = _state.value.copy(kidsArea = !_state.value.kidsArea)
    }

    fun onAccessiblePcdToggle() {
        _state.value = _state.value.copy(accessiblePcd = !_state.value.accessiblePcd)
    }

    fun resetSearch() {
        _state.value = _state.value.copy(hasSearched = false, results = emptyList())
    }

    fun search() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "", hasSearched = false)
            try {
                // Busca restaurantes reais do Firestore
                val snapshot = firestore.collection("restaurants")
                    .whereEqualTo("status", RestaurantStatus.ACTIVE.name)
                    .get()
                    .await()

                val realRestaurants = snapshot.documents.mapNotNull { doc ->
                    try {
                        val featuresMap = doc.get("features") as? Map<*, *>
                        Restaurant(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            cuisineTypes = (doc.get("cuisineTypes") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            establishmentTypes = (doc.get("establishmentTypes") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            priceRange = (doc.getLong("priceRange") ?: 2L).toInt(),
                            rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                            reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt(),
                            distanceKm = (doc.getDouble("distanceKm") ?: 0.0).toFloat(),
                            photos = (doc.get("photos") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            status = RestaurantStatus.ACTIVE,
                            features = com.pentagono.rolematch.domain.model.RestaurantFeatures(
                                petFriendly = featuresMap?.get("petFriendly") as? Boolean ?: false,
                                kidsArea = featuresMap?.get("kidsArea") as? Boolean ?: false,
                                accessiblePcd = featuresMap?.get("accessiblePcd") as? Boolean ?: false,
                                wifi = featuresMap?.get("wifi") as? Boolean ?: false,
                                parking = featuresMap?.get("parking") as? Boolean ?: false,
                                airConditioning = featuresMap?.get("airConditioning") as? Boolean ?: false
                            )
                        )
                    } catch (e: Exception) { null }
                }

                // Busca restaurantes mockados com status ACTIVE
                val mockRestaurants = MockData.restaurants.filter {
                    it.status == RestaurantStatus.ACTIVE
                }

                // Combina os dois sem duplicatas — real tem prioridade
                val realIds = realRestaurants.map { it.id }.toSet()
                val combined = realRestaurants + mockRestaurants.filter { it.id !in realIds }

                val s = _state.value
                val matchResults = combined
                    .filter { r -> applyFilters(r, s) }
                    .map { r -> MatchResult(
                        restaurant = r,
                        score = calculateScore(r, s),
                        matchReasons = generateReasons(r, s)
                    )}
                    .sortedByDescending { it.score }
                    .take(10)

                _state.value = _state.value.copy(
                    results = matchResults,
                    hasSearched = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    error = "Erro ao buscar restaurantes: ${e.message}"
                )
            }
        }
    }

    private fun applyFilters(r: Restaurant, s: MatchState): Boolean {
        val cuisineMatch = s.selectedCuisines.isEmpty() ||
                r.cuisineTypes.any { it in s.selectedCuisines }
        val typeMatch = s.selectedTypes.isEmpty() ||
                r.establishmentTypes.any { it in s.selectedTypes }
        val priceMatch = s.priceRange == null || r.priceRange == s.priceRange
        val petMatch = !s.petFriendly || r.features.petFriendly
        val kidsMatch = !s.kidsArea || r.features.kidsArea
        val pcdMatch = !s.accessiblePcd || r.features.accessiblePcd

        return cuisineMatch && typeMatch && priceMatch && petMatch && kidsMatch && pcdMatch
    }

    private fun calculateScore(r: Restaurant, s: MatchState): Int {
        var score = 0

        // Afinidade de culinária — até 30 pontos
        if (s.selectedCuisines.isNotEmpty()) {
            val matches = r.cuisineTypes.count { it in s.selectedCuisines }
            score += (matches * 10).coerceAtMost(30)
        }

        // Tipo de estabelecimento — até 20 pontos
        if (s.selectedTypes.isNotEmpty()) {
            val matches = r.establishmentTypes.count { it in s.selectedTypes }
            score += (matches * 10).coerceAtMost(20)
        }

        // Faixa de preço compatível — 15 pontos
        if (s.priceRange != null && r.priceRange == s.priceRange) score += 15

        // Avaliação do restaurante — até 20 pontos
        score += (r.rating * 4).toInt().coerceAtMost(20)

        // Características especiais — até 15 pontos
        if (s.petFriendly && r.features.petFriendly) score += 5
        if (s.kidsArea && r.features.kidsArea) score += 5
        if (s.accessiblePcd && r.features.accessiblePcd) score += 5

        return score
    }

    private fun generateReasons(r: Restaurant, s: MatchState): List<String> {
        val reasons = mutableListOf<String>()

        if (s.selectedCuisines.isNotEmpty() && r.cuisineTypes.any { it in s.selectedCuisines }) {
            val match = r.cuisineTypes.firstOrNull { it in s.selectedCuisines }
            if (match != null) reasons.add("Culinária $match combinando")
        }

        if (r.rating >= 4.5f) reasons.add("⭐ Muito bem avaliado")
        else if (r.rating >= 4.0f) reasons.add("⭐ Bem avaliado")

        if (s.priceRange != null && r.priceRange == s.priceRange) reasons.add("💰 Preço ideal para você")

        if (s.petFriendly && r.features.petFriendly) reasons.add("🐾 Aceita pets")
        if (s.kidsArea && r.features.kidsArea) reasons.add("👶 Área kids")
        if (s.accessiblePcd && r.features.accessiblePcd) reasons.add("♿ Acessível")

        if (r.distanceKm > 0f && r.distanceKm <= 1f) reasons.add("📍 Muito perto de você")
        else if (r.distanceKm > 0f && r.distanceKm <= 3f) reasons.add("📍 Perto de você")

        return reasons.take(3)
    }
}