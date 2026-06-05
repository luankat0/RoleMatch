package com.pentagono.rolematch.presentation.screens.user

import androidx.lifecycle.ViewModel
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.domain.model.Restaurant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MatchState(
    val selectedCuisines: Set<String> = emptySet(),
    val selectedTypes: Set<String> = emptySet(),
    val selectedRestrictions: Set<String> = emptySet(),
    val priceRange: Int? = null,
    val maxDistance: Float = 5f,
    val petFriendly: Boolean = false,
    val kidsArea: Boolean = false,
    val accessiblePcd: Boolean = false,
    val results: List<Restaurant> = emptyList(),
    val hasSearched: Boolean = false
)

class MatchViewModel : ViewModel() {

    private val _state = MutableStateFlow(MatchState())
    val state: StateFlow<MatchState> = _state.asStateFlow()

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

    // ✅ Bug 20: filtra restaurantes com base nas preferências selecionadas
    fun search() {
        val s = _state.value
        val results = MockData.restaurants.filter { r ->

            // Culinária: se nada selecionado, passa todos
            val cuisineMatch = s.selectedCuisines.isEmpty() ||
                    r.cuisineTypes.any { it in s.selectedCuisines }

            // Tipo de estabelecimento
            val typeMatch = s.selectedTypes.isEmpty() ||
                    r.establishmentTypes.any { it in s.selectedTypes }

            // Distância
            val distanceMatch = r.distanceKm <= s.maxDistance

            // Faixa de preço
            val priceMatch = s.priceRange == null || r.priceRange == s.priceRange

            // Pet friendly
            val petMatch = !s.petFriendly || r.features.petFriendly

            // Área kids
            val kidsMatch = !s.kidsArea || r.features.kidsArea

            // Acessível PcD
            val pcdMatch = !s.accessiblePcd || r.features.accessiblePcd

            cuisineMatch && typeMatch && distanceMatch &&
                    priceMatch && petMatch && kidsMatch && pcdMatch

        }.sortedByDescending { r ->
            // Ordena por relevância: pontuação baseada em quantos critérios batem
            var score = 0
            if (s.selectedCuisines.isNotEmpty() &&
                r.cuisineTypes.any { it in s.selectedCuisines }) score += 3
            if (s.selectedTypes.isNotEmpty() &&
                r.establishmentTypes.any { it in s.selectedTypes }) score += 2
            if (s.priceRange != null && r.priceRange == s.priceRange) score += 2
            score += (r.rating * 10).toInt()
            score
        }

        _state.value = _state.value.copy(
            results = results,
            hasSearched = true
        )
    }
}