package com.pentagono.rolematch.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.domain.model.Address
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.model.RestaurantFeatures
import com.pentagono.rolematch.domain.model.RestaurantStatus
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── State ────────────────────────────────────────────────────────────────────

data class HomeUiState(
    val allRestaurants: List<Restaurant> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val selectedCategory: String = "Todos",
    val isLoading: Boolean = true,
    val favorites: Set<String> = emptySet(),
    val showFilters: Boolean = false
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    val categories = listOf(
        "Todos", "Restaurantes", "Bistrôs", "Bar",
        "Pizzaria", "Cafeteria", "Japonesa", "Italiana",
        "Brasileira", "Hamburgeria"
    )

    init { loadRestaurants() }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Busca restaurantes aprovados do Firestore
                val snapshot = firestore.collection("restaurants")
                    .whereEqualTo("status", RestaurantStatus.ACTIVE.name)
                    .get()
                    .await()

                val firestoreRestaurants = snapshot.documents.mapNotNull { doc ->
                    try {
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

                        Restaurant(
                            id = doc.id,
                            ownerId = doc.getString("ownerId") ?: "",
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
                            status = RestaurantStatus.ACTIVE,
                            rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                            reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt(),
                            isOpen = true
                        )
                    } catch (e: Exception) { null }
                }

                // Mistura: Firestore primeiro, depois MockData (evita duplicatas por ID)
                val firestoreIds = firestoreRestaurants.map { it.id }.toSet()
                val mockFiltered = MockData.restaurants.filter { it.id !in firestoreIds }
                val combined = firestoreRestaurants + mockFiltered

                _state.value = _state.value.copy(
                    allRestaurants = combined,
                    restaurants = combined,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Se falhar, usa só o MockData
                _state.value = _state.value.copy(
                    allRestaurants = MockData.restaurants,
                    restaurants = MockData.restaurants,
                    isLoading = false
                )
            }
        }
    }

    fun onCategorySelected(cat: String) {
        val filtered = if (cat == "Todos") {
            _state.value.allRestaurants
        } else {
            _state.value.allRestaurants.filter { r ->
                r.establishmentTypes.any { it.contains(cat, ignoreCase = true) } ||
                        r.cuisineTypes.any { it.contains(cat, ignoreCase = true) }
            }
        }
        _state.value = _state.value.copy(selectedCategory = cat, restaurants = filtered)
    }

    fun toggleFavorite(restaurantId: String) {
        val current = _state.value.favorites
        _state.value = _state.value.copy(
            favorites = if (restaurantId in current) current - restaurantId else current + restaurantId
        )
    }

    fun toggleFilters() {
        _state.value = _state.value.copy(showFilters = !_state.value.showFilters)
    }

    fun applyAdvancedFilter(
        onlyOpen: Boolean = false,
        maxDistance: Float = 10f,
        minRating: Float = 0f,
        priceRange: Int? = null
    ) {
        val filtered = _state.value.allRestaurants.filter { r ->
            val categoryMatch = if (_state.value.selectedCategory == "Todos") true
            else r.establishmentTypes.any { it.contains(_state.value.selectedCategory, ignoreCase = true) } ||
                    r.cuisineTypes.any { it.contains(_state.value.selectedCategory, ignoreCase = true) }
            val openMatch = if (onlyOpen) r.isOpen else true
            val distanceMatch = r.distanceKm <= maxDistance
            val ratingMatch = r.rating >= minRating
            val priceMatch = if (priceRange != null) r.priceRange == priceRange else true
            categoryMatch && openMatch && distanceMatch && ratingMatch && priceMatch
        }
        _state.value = _state.value.copy(restaurants = filtered, showFilters = false)
    }

    fun getFavoriteRestaurants(): List<Restaurant> {
        return _state.value.allRestaurants.filter { it.id in _state.value.favorites }
    }
}

// ─── HomeScreen ───────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToMatch: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    viewModel: HomeViewModel
) {
    val state by viewModel.state.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var filterOnlyOpen by remember { mutableStateOf(false) }
    var filterMaxDistance by remember { mutableStateOf(10f) }
    var filterMinRating by remember { mutableStateOf(0f) }
    var filterPriceRange by remember { mutableStateOf<Int?>(null) }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filtros Avançados", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Apenas abertos agora", modifier = Modifier.weight(1f))
                        Switch(checked = filterOnlyOpen, onCheckedChange = { filterOnlyOpen = it }, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen))
                    }
                    Text("Distância máxima: ${filterMaxDistance.toInt()}km", fontWeight = FontWeight.Medium)
                    Slider(value = filterMaxDistance, onValueChange = { filterMaxDistance = it }, valueRange = 0.5f..10f, colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen))
                    Text("Avaliação mínima: ${String.format("%.1f", filterMinRating)}★", fontWeight = FontWeight.Medium)
                    Slider(value = filterMinRating, onValueChange = { filterMinRating = it }, valueRange = 0f..5f, colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen))
                    Text("Faixa de preço", fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(null to "Todos", 1 to "$", 2 to "$$", 3 to "$$$", 4 to "$$$$").forEach { (value, label) ->
                            FilterChip(
                                selected = filterPriceRange == value,
                                onClick = { filterPriceRange = value },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = androidx.compose.ui.graphics.Color.White)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.applyAdvancedFilter(filterOnlyOpen, filterMaxDistance, filterMinRating, filterPriceRange); showFilterDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Aplicar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFilterDialog = false }, shape = RoundedCornerShape(10.dp)) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RoleMatchLogo()
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, "Notificações", tint = TextMuted)
                    }
                }
            }
        },
        bottomBar = {
            UserBottomBar(currentRoute = "user_home") { route ->
                when (route) {
                    "search" -> onNavigateToSearch()
                    "match" -> onNavigateToMatch()
                    "coupons" -> onNavigateToCoupons()
                    "user_profile" -> onNavigateToProfile()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.categories.forEach { cat ->
                        SelectableChip(label = cat, selected = state.selectedCategory == cat, onClick = { viewModel.onCategorySelected(cat) })
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isLoading) {
                        Text("Carregando restaurantes...", fontSize = 14.sp, color = TextMuted)
                    } else {
                        Text("${state.restaurants.size} restaurantes encontrados", fontSize = 14.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    }
                    TextButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filtros avançados", color = PrimaryGreen, fontSize = 13.sp)
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            } else {
                items(state.restaurants) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        coupons = MockData.getCouponsForRestaurant(restaurant.id),
                        onClick = { onNavigateToRestaurant(restaurant.id) },
                        onFavorite = { viewModel.toggleFavorite(restaurant.id) },
                        isFavorite = restaurant.id in state.favorites,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}