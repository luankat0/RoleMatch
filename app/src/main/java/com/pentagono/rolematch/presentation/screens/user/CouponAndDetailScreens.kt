package com.pentagono.rolematch.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.data.repository.RepositoryProvider
import com.pentagono.rolematch.domain.model.Address
import com.pentagono.rolematch.domain.model.Coupon
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.model.RestaurantFeatures
import com.pentagono.rolematch.domain.model.RestaurantStatus
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.tasks.await
import java.util.Date

@Composable
fun CouponScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToMatch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCouponDetails: (String) -> Unit,
    onNavigateToRestaurant: (String) -> Unit
) {
    // Busca restaurantes com cupons — mock + Firestore
    val mockRestaurantsWithCoupons = MockData.restaurants.filter { restaurant ->
        MockData.getCouponsForRestaurant(restaurant.id).isNotEmpty()
    }
    var firestoreCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val snap = FirebaseFirestore.getInstance()
                .collection("coupons").whereEqualTo("active", true).get().await()
            val restaurantIds = snap.documents.mapNotNull { it.getString("restaurantId") }.toSet()
            firestoreCount = restaurantIds.size
        } catch (e: Exception) { }
    }

    val totalCount = mockRestaurantsWithCoupons.size + firestoreCount

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text("Cupons & Promoções", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("$totalCount restaurantes com cupons disponíveis", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        bottomBar = {
            UserBottomBar(currentRoute = "coupons") { route ->
                when (route) {
                    "user_home" -> onNavigateToHome()
                    "search" -> onNavigateToSearch()
                    "match" -> onNavigateToMatch()
                    "user_profile" -> onNavigateToProfile()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockRestaurantsWithCoupons) { restaurant ->
                val coupons = MockData.getCouponsForRestaurant(restaurant.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(listOfNotNull(restaurant.cuisineTypes.firstOrNull(), restaurant.establishmentTypes.firstOrNull()).joinToString(", "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                    Text("${restaurant.distanceKm}km", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    StatusBadge(restaurant.isOpen)
                                }
                            }
                            Icon(Icons.Default.FavoriteBorder, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("${coupons.size} CUPOM${if (coupons.size > 1) "S" else ""} DISPONÍVEL${if (coupons.size > 1) "IS" else ""}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscountRed)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            coupons.take(2).forEach { coupon ->
                                OutlinedButton(
                                    onClick = { onNavigateToCouponDetails(coupon.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(coupon.title.take(15), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                }
                            }
                            if (coupons.size < 2) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(onClick = { onNavigateToRestaurant(restaurant.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen)) {
                            Text("Ver Restaurante", color = PrimaryGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    onBack: () -> Unit,
    onNavigateToReservation: (String) -> Unit,
    onNavigateToCouponDetails: (String) -> Unit
) {
    // Busca do MockData primeiro, se não achar busca do Firestore
    var restaurant by remember { mutableStateOf<Restaurant?>(MockData.getRestaurantById(restaurantId)) }
    var coupons by remember { mutableStateOf(MockData.getCouponsForRestaurant(restaurantId)) }
    val reviews = MockData.reviews.filter { it.restaurantId == restaurantId }
    var activeTab by remember { mutableStateOf("sobre") }
    var isLoading by remember { mutableStateOf(restaurant == null) }

    // Se não achou no MockData, busca no Firestore
    // Sempre busca cupons do Firestore para restaurantes reais
    val isMockRestaurant = remember { MockData.getRestaurantById(restaurantId) != null }

    LaunchedEffect(restaurantId) {
        // Busca cupons do Firestore para restaurantes não-mock
        if (!isMockRestaurant) {
            android.util.Log.d("CouponDebug", "Buscando cupons para restaurantId: $restaurantId")
            val couponResult = RepositoryProvider.coupon.getCouponsByRestaurant(restaurantId)
            couponResult.fold(
                onSuccess = { list ->
                    android.util.Log.d("CouponDebug", "Cupons encontrados: ${list.size}")
                    coupons = list
                },
                onFailure = { e ->
                    android.util.Log.e("CouponDebug", "Erro: ${e.message}")
                }
            )
        } else {
            android.util.Log.d("CouponDebug", "Restaurante mock, pulando busca de cupons")
        }

        if (restaurant == null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val doc = firestore.collection("restaurants").document(restaurantId).get().await()
                if (doc.exists()) {
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

                    val status = try { RestaurantStatus.valueOf(doc.getString("status") ?: "PENDING_REVIEW") }
                    catch (e: Exception) { RestaurantStatus.PENDING_REVIEW }

                    restaurant = Restaurant(
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
                        status = status,
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt()
                    )
                }
            } catch (e: Exception) { }
            isLoading = false
        } else {
            // Restaurante mock — não precisa carregar, só marca como não-loading
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryGreen)
        }
        return
    }

    val r = restaurant ?: MockData.restaurants.first()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    AsyncImage(
                        model = r.photos.firstOrNull() ?: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600",
                        contentDescription = r.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(12.dp).statusBarsPadding()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-20).dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(r.name, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${r.cuisineTypes.joinToString(", ")} • ${r.establishmentTypes.joinToString(", ")}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                            StatusBadge(r.isOpen)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RatingBadge(r.rating)
                            Text("(${r.reviewCount} avaliações)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (r.distanceKm > 0) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Text("${r.distanceKm}km", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        val tags = r.cuisineTypes + r.establishmentTypes +
                            listOfNotNull(
                                if (r.features.petFriendly) "🐾 Pet Friendly" else null,
                                if (r.features.kidsArea) "👶 Área Kids" else null
                            )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            tags.chunked(3).forEach { rowTags ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowTags.forEach { tag ->
                                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                            Text(tag, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PrimaryButton("Fazer Reserva", onClick = { onNavigateToReservation(restaurantId) }, modifier = Modifier.fillMaxWidth())
                            SecondaryButton("Ver Cardápio", onClick = {}, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            if (coupons.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, null, tint = DiscountRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cupons Disponíveis (${coupons.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                items(coupons) { coupon ->
                    CouponCard(coupon = coupon, restaurant = null, onDetails = { onNavigateToCouponDetails(coupon.id) }, onRedeem = { onNavigateToCouponDetails(coupon.id) }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                TabRow(
                    selectedTabIndex = listOf("sobre", "galeria", "avaliacoes").indexOf(activeTab),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryGreen,
                    indicator = { tabPositions ->
                        val idx = listOf("sobre", "galeria", "avaliacoes").indexOf(activeTab)
                        if (idx in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[idx]), color = PrimaryGreen)
                        }
                    }
                ) {
                    listOf("Sobre" to "sobre", "Galeria" to "galeria", "Avaliações" to "avaliacoes").forEach { (label, key) ->
                        Tab(selected = activeTab == key, onClick = { activeTab = key }, text = { Text(label, fontWeight = if (activeTab == key) FontWeight.Bold else FontWeight.Normal) })
                    }
                }
            }

            item {
                when (activeTab) {
                    "sobre" -> AboutTab(r)
                    "galeria" -> GalleryTab(r)
                    "avaliacoes" -> ReviewsTab(reviews)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AboutTab(restaurant: Restaurant) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (restaurant.description.isNotEmpty()) {
            Text("Nossa História", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(restaurant.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        if (restaurant.phone.isNotEmpty() || restaurant.instagram.isNotEmpty()) {
            Text("Contato & Redes Sociais", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            if (restaurant.phone.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(restaurant.phone, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            if (restaurant.instagram.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(restaurant.instagram, fontSize = 14.sp, color = PrimaryGreen)
                }
            }
        }
        if (restaurant.address.street.isNotEmpty()) {
            Text("Endereço", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(restaurant.address.formatted(), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (restaurant.mealVouchers.isNotEmpty()) {
            Text("Vales Refeição Aceitos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                restaurant.mealVouchers.forEach { voucher ->
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(voucher, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryTab(restaurant: Restaurant) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Fotos do Estabelecimento (${restaurant.photos.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(10.dp))
        if (restaurant.photos.isEmpty()) {
            Text("Nenhuma foto disponível.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        } else {
            val rows = restaurant.photos.chunked(2)
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { photo ->
                        AsyncImage(model = photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(12.dp)))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReviewsTab(reviews: List<com.pentagono.rolematch.domain.model.Review>) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (reviews.isEmpty()) {
            Text("Nenhuma avaliação ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            reviews.forEach { review -> ReviewCard(review = review, modifier = Modifier.padding(bottom = 8.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Deixar uma avaliação", onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}
