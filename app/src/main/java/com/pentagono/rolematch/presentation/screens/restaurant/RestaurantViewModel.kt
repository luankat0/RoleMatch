package com.pentagono.rolematch.presentation.screens.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.domain.model.Address
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.model.RestaurantFeatures
import com.pentagono.rolematch.domain.model.RestaurantStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RestaurantState(
    val restaurant: Restaurant? = null,
    val isLoading: Boolean = true,
    val error: String = ""
)

class RestaurantViewModel : ViewModel() {

    private val _state = MutableStateFlow(RestaurantState())
    val state: StateFlow<RestaurantState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadRestaurant()
    }

    fun loadRestaurant() {
        viewModelScope.launch {
            _state.value = RestaurantState(isLoading = true)
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _state.value = RestaurantState(isLoading = false, error = "Usuário não logado")
                return@launch
            }
            try {
                val query = firestore
                    .collection("restaurants")
                    .whereEqualTo("ownerId", uid)
                    .limit(1)
                    .get()
                    .await()

                val doc = query.documents.firstOrNull()
                if (doc == null) {
                    _state.value = RestaurantState(isLoading = false, error = "Restaurante não encontrado")
                    return@launch
                }

                val address = (doc.get("address") as? Map<*, *>)?.let { a ->
                    Address(
                        cep = a["cep"] as? String ?: "",
                        street = a["street"] as? String ?: "",
                        number = a["number"] as? String ?: "",
                        neighborhood = a["neighborhood"] as? String ?: "",
                        city = a["city"] as? String ?: "",
                        state = a["state"] as? String ?: ""
                    )
                } ?: Address()

                val features = (doc.get("features") as? Map<*, *>)?.let { f ->
                    RestaurantFeatures(
                        petFriendly = f["petFriendly"] as? Boolean ?: false,
                        kidsArea = f["kidsArea"] as? Boolean ?: false,
                        acceptsVr = f["acceptsVr"] as? Boolean ?: false,
                        accessiblePcd = f["accessiblePcd"] as? Boolean ?: false
                    )
                } ?: RestaurantFeatures()

                val statusStr = doc.getString("status") ?: "PENDING_REVIEW"
                val status = try { RestaurantStatus.valueOf(statusStr) }
                catch (e: Exception) { RestaurantStatus.PENDING_REVIEW }

                val restaurant = Restaurant(
                    id = doc.id,
                    ownerId = doc.getString("ownerId") ?: uid,
                    name = doc.getString("name") ?: "",
                    cnpj = doc.getString("cnpj") ?: "",
                    description = doc.getString("description") ?: "",
                    establishmentTypes = (doc.get("establishmentTypes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    cuisineTypes = (doc.get("cuisineTypes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    address = address,
                    phone = doc.getString("phone") ?: "",
                    whatsapp = doc.getString("whatsapp") ?: "",
                    instagram = doc.getString("instagram") ?: "",
                    menuLink = doc.getString("menuLink") ?: "",
                    features = features,
                    acceptsReservations = doc.getBoolean("acceptsReservations") ?: true,
                    photos = (doc.get("photos") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    status = status,
                    rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                    reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt()
                )

                _state.value = RestaurantState(restaurant = restaurant, isLoading = false)
            } catch (e: Exception) {
                _state.value = RestaurantState(
                    isLoading = false,
                    error = "Erro ao carregar dados do restaurante"
                )
            }
        }
    }
}