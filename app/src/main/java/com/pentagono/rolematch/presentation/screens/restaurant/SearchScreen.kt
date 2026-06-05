package com.pentagono.rolematch.presentation.screens.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.DarkGreen
import com.pentagono.rolematch.core.theme.LightGreen
import com.pentagono.rolematch.core.theme.PrimaryGreen
import com.pentagono.rolematch.core.theme.TextMuted
import com.pentagono.rolematch.presentation.components.UserBottomBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToMatch: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categories = MockData.cuisineCategories

    val filteredRestaurants = remember(query, selectedCategory) {
        MockData.restaurants.filter { restaurant ->
            val matchesQuery = query.isBlank() ||
                    restaurant.name.contains(query, ignoreCase = true)

            val matchesCategory = selectedCategory == null ||
                    restaurant.name.contains(selectedCategory.orEmpty(), ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                title = "Buscar",
                subtitle = "Encontre restaurantes, bares e experiências",
                onBack = onBack
            )
        },
        bottomBar = {
            UserBottomBar(currentRoute = "search") { route ->
                when (route) {
                    "user_home" -> onNavigateToHome()
                    "match" -> onNavigateToMatch()
                    "coupons" -> onNavigateToCoupons()
                    "user_profile" -> onNavigateToProfile()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = RMIcons.Search,
                            contentDescription = null
                        )
                    },
                    placeholder = {
                        Text("Buscar por nome do restaurante")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                Text(
                    text = "Categorias",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Todos") }
                    )

                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) {
                                    null
                                } else {
                                    category
                                }
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                Text(
                    text = "${filteredRestaurants.size} resultado(s) encontrado(s)",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            items(
                items = filteredRestaurants,
                key = { restaurant -> restaurant.id }
            ) { restaurant ->
                SearchRestaurantCard(
                    name = restaurant.name,
                    onClick = {
                        onNavigateToRestaurant(restaurant.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MostFamousScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    val restaurants = MockData.restaurants
        .take(10)
        .map { restaurant ->
            RestaurantSearchItem(
                id = restaurant.id,
                name = restaurant.name
            )
        }

    RestaurantCategoryScreen(
        title = "Mais famosos",
        subtitle = "Restaurantes mais conhecidos pelos usuários",
        restaurants = restaurants,
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

@Composable
fun CrowdFavoritesScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    val restaurants = MockData.restaurants
        .takeLast(10)
        .map { restaurant ->
            RestaurantSearchItem(
                id = restaurant.id,
                name = restaurant.name
            )
        }

    RestaurantCategoryScreen(
        title = "Queridinhos do público",
        subtitle = "Lugares bem avaliados pela comunidade",
        restaurants = restaurants,
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

@Composable
fun NearbyRestaurantsScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    val restaurants = MockData.restaurants
        .take(8)
        .map { restaurant ->
            RestaurantSearchItem(
                id = restaurant.id,
                name = restaurant.name
            )
        }

    RestaurantCategoryScreen(
        title = "Restaurantes próximos",
        subtitle = "Sugestões próximas da sua localização",
        restaurants = restaurants,
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

@Composable
fun BestForEventsScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    val restaurants = MockData.restaurants
        .take(8)
        .map { restaurant ->
            RestaurantSearchItem(
                id = restaurant.id,
                name = restaurant.name
            )
        }

    RestaurantCategoryScreen(
        title = "Melhores para eventos",
        subtitle = "Boas opções para aniversários, encontros e comemorações",
        restaurants = restaurants,
        onBack = onBack,
        onNavigateToRestaurant = onNavigateToRestaurant
    )
}

private data class RestaurantSearchItem(
    val id: String,
    val name: String
)

@Composable
private fun RestaurantCategoryScreen(
    title: String,
    subtitle: String,
    restaurants: List<RestaurantSearchItem>,
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    Scaffold(
        topBar = {
            SearchTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "${restaurants.size} restaurante(s)",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            items(
                items = restaurants,
                key = { restaurant -> restaurant.id }
            ) { restaurant ->
                SearchRestaurantCard(
                    name = restaurant.name,
                    onClick = {
                        onNavigateToRestaurant(restaurant.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = RMIcons.ArrowBack,
                    contentDescription = "Voltar"
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun SearchRestaurantCard(
    name: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(LightGreen, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "R",
                    color = DarkGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = RMIcons.Star,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = " 4.8 • Aberto agora",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = RMIcons.LocationOn,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )

                    Text(
                        text = " Próximo de você",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            AssistChip(
                onClick = onClick,
                label = {
                    Text("Ver")
                }
            )
        }
    }
}