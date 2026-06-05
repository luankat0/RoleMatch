package com.pentagono.rolematch.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.presentation.components.*

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToMostFamous: () -> Unit,
    onNavigateToCrowdFavorites: () -> Unit,
    onNavigateToNearby: () -> Unit,
    onNavigateToBestForEvents: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToMatch: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults = remember(query) {
        if (query.isBlank()) emptyList()
        else MockData.restaurants.filter { r ->
            r.name.contains(query, ignoreCase = true) ||
                    r.cuisineTypes.any { it.contains(query, ignoreCase = true) } ||
                    r.establishmentTypes.any { it.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Text("Buscar", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        },
        bottomBar = {
            UserBottomBar(currentRoute = "search") { route ->
                when (route) {
                    "user_home" -> onBack()
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            "Restaurante, tipo de comida...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    "Limpar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = PrimaryGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotBlank()) {
                if (searchResults.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Nenhum resultado para \"$query\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            "${searchResults.size} resultado${if (searchResults.size > 1) "s" else ""} encontrado${if (searchResults.size > 1) "s" else ""}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(searchResults) { restaurant ->
                        SearchRestaurantCard(
                            restaurant = restaurant,
                            coupons = MockData.getCouponsForRestaurant(restaurant.id),
                            onClick = { onNavigateToRestaurant(restaurant.id) }
                        )
                    }
                }
            } else {
                item {
                    Text("Categorias Especiais", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SpecialCategoryCard(
                            label = "Mais Famosos",
                            icon = Icons.Default.TrendingUp,
                            gradient = listOf(Color(0xFFE53935), Color(0xFFEF9A9A)),
                            onClick = onNavigateToMostFamous,
                            modifier = Modifier.weight(1f)
                        )
                        SpecialCategoryCard(
                            label = "Favoritos da Galera",
                            icon = Icons.Default.Favorite,
                            gradient = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)),
                            onClick = onNavigateToCrowdFavorites,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SpecialCategoryCard(
                            label = "Mais Perto de Você",
                            icon = Icons.Default.NearMe,
                            gradient = listOf(Color(0xFF1565C0), Color(0xFF42A5F5)),
                            onClick = onNavigateToNearby,
                            modifier = Modifier.weight(1f)
                        )
                        SpecialCategoryCard(
                            label = "Melhores para Eventos",
                            icon = Icons.Default.Event,
                            gradient = listOf(Color(0xFF6A1B9A), Color(0xFFCE93D8)),
                            onClick = onNavigateToBestForEvents,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Restaurantes em Destaque",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Patrocinado",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                items(MockData.restaurants.take(2)) { restaurant ->
                    FeaturedRestaurantCard(
                        restaurant = restaurant,
                        onClick = { onNavigateToRestaurant(restaurant.id) }
                    )
                }

                item {
                    Text("Buscas populares", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MockData.popularSearches.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowItems.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .clickable { query = tag }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        // ✅ modo escuro: usa cor do tema
                                        Text(
                                            tag,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialCategoryCard(
    label: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FeaturedRestaurantCard(
    restaurant: com.pentagono.rolematch.domain.model.Restaurant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    restaurant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    restaurant.cuisineTypes.joinToString(", "),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "${restaurant.distanceKm}km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatusBadge(restaurant.isOpen)
                }
            }
            RatingBadge(restaurant.rating)
        }
    }
}

@Composable
fun MostFamousScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    RestaurantListScreen(
        title = "Mais Famosos",
        titleColor = DiscountRed,
        restaurants = MockData.restaurants.sortedByDescending { it.rating },
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

@Composable
fun CrowdFavoritesScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    RestaurantListScreen(
        title = "Favoritos da Galera",
        titleColor = PrimaryGreen,
        restaurants = MockData.restaurants.sortedByDescending { it.reviewCount },
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

@Composable
fun NearbyRestaurantsScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    RestaurantListScreen(
        title = "Mais Perto de Você",
        titleColor = BlueAccent,
        restaurants = MockData.restaurants.sortedBy { it.distanceKm },
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

@Composable
fun BestForEventsScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    RestaurantListScreen(
        title = "Melhores para Eventos",
        titleColor = PurpleAccent,
        restaurants = MockData.restaurants.filter {
            it.features.kidsArea || it.priceRange >= 3
        }.sortedByDescending { it.rating },
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

@Composable
private fun RestaurantListScreen(
    title: String,
    titleColor: Color,
    restaurants: List<com.pentagono.rolematch.domain.model.Restaurant>,
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = titleColor)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    "${restaurants.size} restaurantes encontrados",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(restaurants) { restaurant ->
                SearchRestaurantCard(
                    restaurant = restaurant,
                    coupons = MockData.getCouponsForRestaurant(restaurant.id),
                    onClick = { onNavigateToRestaurant(restaurant.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}