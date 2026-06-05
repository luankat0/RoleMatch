package com.pentagono.rolematch.presentation.screens.user

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.data.repository.RepositoryProvider
import com.pentagono.rolematch.domain.model.Reservation
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MatchPreferencesScreen(
    onBack: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: MatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Column {
                        Text("Match Perfeito", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Encontre o restaurante ideal para você", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        bottomBar = {
            Column {
                GradientMatchButton(
                    text = "Buscar Match",
                    onClick = { viewModel.search(); onNavigateToResults() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
                UserBottomBar(currentRoute = "match") { route ->
                    when (route) {
                        "user_home" -> onNavigateToHome()
                        "coupons" -> onNavigateToCoupons()
                        "user_profile" -> onNavigateToProfile()
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PreferenceSection(title = "Tipo de comida") {
                ChipGrid(items = MockData.cuisineCategories, selected = state.selectedCuisines, onToggle = { viewModel.onCuisineToggle(it) })
            }
            PreferenceSection(title = "Tipo de estabelecimento") {
                ChipGrid(items = MockData.establishmentTypes, selected = state.selectedTypes, onToggle = { viewModel.onTypeToggle(it) })
            }
            PreferenceSection(title = "Restrições alimentares") {
                Text("Selecione as restrições alimentares para encontrar restaurantes seguros para você", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                ChipGrid(items = MockData.dietaryRestrictions, selected = state.selectedRestrictions, onToggle = { viewModel.onRestrictionToggle(it) })
            }
            PreferenceSection(title = "Quanto você quer gastar?") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Econômico", 2 to "Moderado", 3 to "Caro", 4 to "Muito caro").forEach { (value, label) ->
                        Card(
                            modifier = Modifier.weight(1f).clickable { viewModel.onPriceRangeChange(if (state.priceRange == value) null else value) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (state.priceRange == value) PrimaryGreen else MaterialTheme.colorScheme.surface)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                Text("$".repeat(value), fontWeight = FontWeight.Bold, color = if (state.priceRange == value) Color.White else MaterialTheme.colorScheme.onSurface)
                                Text(label, fontSize = 10.sp, color = if (state.priceRange == value) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            PreferenceSection(title = "Distância máxima: ${state.maxDistance.toInt()}km") {
                Slider(value = state.maxDistance, onValueChange = { viewModel.onMaxDistanceChange(it) }, valueRange = 0.5f..10f, colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen), modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.5km", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("10km", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            PreferenceSection(title = "Características especiais") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableChip("🐾 Pet", state.petFriendly, { viewModel.onPetFriendlyToggle() })
                    SelectableChip("👶 Kids", state.kidsArea, { viewModel.onKidsAreaToggle() })
                    SelectableChip("♿ PcD", state.accessiblePcd, { viewModel.onAccessiblePcdToggle() })
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PreferenceSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ChipGrid(items: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item -> SelectableChip(label = item, selected = item in selected, onClick = { onToggle(item) }, modifier = Modifier.weight(1f)) }
                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToMatch: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onLogout: () -> Unit,
    favoriteRestaurantIds: Set<String> = emptySet()
) {
    var activeTab by remember { mutableStateOf("reservas") }
    val favorites = MockData.restaurants.filter { it.id in favoriteRestaurantIds }

    val userViewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val userState by userViewModel.state.collectAsState()
    val user = userState.user

    // Reservas reais do Firestore
    val reservationRepo = remember { RepositoryProvider.reservation }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var reservationsLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user?.id) {
        val uid = user?.id ?: FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            reservationsLoading = true
            val result = reservationRepo.getUserReservations(uid)
            result.fold(
                onSuccess = { reservations = it },
                onFailure = { reservations = emptyList() }
            )
            reservationsLoading = false
        }
    }

    val profilePhotoContext = LocalContext.current
    var isUploadingPhoto by remember { mutableStateOf(false) }
    val profilePhotoScope = rememberCoroutineScope()

    val profilePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            profilePhotoScope.launch {
                isUploadingPhoto = true
                val uid = user?.id ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val result = com.pentagono.rolematch.core.common.ImageUploadHelper.uploadUserProfilePhoto(profilePhotoContext, it)
                result.fold(
                    onSuccess = { url ->
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(uid)
                            .update("photoUrl", url)
                            .await()
                        userViewModel.reload()
                    },
                    onFailure = {}
                )
                isUploadingPhoto = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            UserBottomBar(currentRoute = "user_profile") { route ->
                when (route) {
                    "user_home" -> onNavigateToHome()
                    "search" -> onNavigateToSearch()
                    "match" -> onNavigateToMatch()
                    "coupons" -> onNavigateToCoupons()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (userState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).align(Alignment.TopCenter)) {
                        Surface(color = DiscountRed, modifier = Modifier.fillMaxSize()) {}
                        Row(
                            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Meu Perfil", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Default.Settings, "Configurações", tint = Color.White)
                            }
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        Box(modifier = Modifier.size(80.dp)) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(80.dp)) {
                                if (!user?.photoUrl.isNullOrEmpty()) {
                                    coil.compose.AsyncImage(
                                        model = user?.photoUrl,
                                        contentDescription = "Foto de perfil",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(user?.name?.firstOrNull()?.toString() ?: "U", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = PrimaryGreen)
                                    }
                                }
                            }
                            // Botão de editar foto
                            Surface(
                                shape = CircleShape,
                                color = PrimaryGreen,
                                modifier = Modifier.size(24.dp).align(Alignment.BottomEnd).clickable { profilePhotoPickerLauncher.launch("image/*") }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        if (isUploadingPhoto) {
                            CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(80.dp), strokeWidth = 3.dp)
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(user?.name ?: "Carregando...", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(user?.email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    if (!user?.phone.isNullOrEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(user?.phone ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(title = "❤️ Favoritos (${favorites.size})", actionLabel = if (favorites.size > 6) "Ver todos" else "")
                        Spacer(Modifier.height(12.dp))
                        if (favorites.isEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FavoriteBorder, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Você ainda não favoritou nenhum restaurante", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            val rows = favorites.take(6).chunked(3)
                            rows.forEachIndexed { rowIndex, row ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { restaurant ->
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer).clickable { onNavigateToRestaurant(restaurant.id) })
                                            Spacer(Modifier.height(4.dp))
                                            Text(restaurant.name.split(" ").take(2).joinToString(" "), fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                                if (rowIndex < rows.size - 1) Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TabButton("📅 Reservas", activeTab == "reservas") { activeTab = "reservas" }
                            TabButton("⭐ Avaliações", activeTab == "avaliacoes") { activeTab = "avaliacoes" }
                        }
                        Spacer(Modifier.height(12.dp))

                        if (activeTab == "reservas") {
                            when {
                                reservationsLoading -> {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                                    }
                                }
                                reservations.isEmpty() -> {
                                    Text("Nenhuma reserva ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                }
                                else -> {
                                    reservations.forEach { reservation ->
                                        ReservationCard(
                                            restaurantName = reservation.restaurantName,
                                            date = reservation.date,
                                            time = reservation.time,
                                            peopleCount = reservation.peopleCount,
                                            status = reservation.status,
                                            onViewRestaurant = { onNavigateToRestaurant(reservation.restaurantId) },
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            val reviews = MockData.reviews
                            if (reviews.isEmpty()) {
                                Text("Nenhuma avaliação ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            } else {
                                reviews.forEach { review ->
                                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                        val restaurant = MockData.getRestaurantById(review.restaurantId)
                                        Text(restaurant?.name ?: "Restaurante", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.height(4.dp))
                                        ReviewCard(review = review)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ListItem(
                            headlineContent = { Text("Configurações") },
                            leadingContent = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.clickable { onNavigateToSettings() }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ListItem(
                            headlineContent = { Text("Sair", color = DiscountRed) },
                            leadingContent = { Icon(RMIcons.ExitToApp, null, tint = DiscountRed) },
                            modifier = Modifier.clickable { onLogout() }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) PrimaryGreen else MaterialTheme.colorScheme.surface, contentColor = if (selected) Color.White else TextMuted),
        shape = RoundedCornerShape(10.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}