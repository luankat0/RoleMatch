package com.pentagono.rolematch.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.domain.model.Coupon
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.model.ReservationStatus
import com.pentagono.rolematch.domain.model.Review

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    coupons: List<Coupon> = emptyList(),
    onClick: () -> Unit,
    onFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = restaurant.photos.firstOrNull()
                        ?: "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400",
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                if (coupons.isNotEmpty()) {
                    Box(Modifier.align(Alignment.TopStart).padding(10.dp)) {
                        CouponBadge(coupons.size)
                    }
                }
                Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                    RatingBadge(restaurant.rating)
                }
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        // ✅ modo escuro: surface do tema em vez de Color.White
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) DiscountRed else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                    StatusBadge(restaurant.isOpen)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    restaurant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append(restaurant.cuisineTypes.joinToString(", "))
                        if (restaurant.establishmentTypes.isNotEmpty()) {
                            append(" • ${restaurant.establishmentTypes.joinToString(", ")}")
                        }
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "${restaurant.distanceKm}km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PriceRangeBadge(restaurant.priceRange)
                }
            }
        }
    }
}

@Composable
fun SearchRestaurantCard(
    restaurant: Restaurant,
    coupons: List<Coupon> = emptyList(),
    onClick: () -> Unit,
    onFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                AsyncImage(
                    model = restaurant.photos.firstOrNull()
                        ?: "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400",
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                if (coupons.isNotEmpty()) {
                    Box(Modifier.align(Alignment.TopStart).padding(8.dp)) {
                        CouponBadge(coupons.size)
                    }
                }
                Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    RatingBadge(restaurant.rating)
                }
                Box(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                    StatusBadge(restaurant.isOpen)
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        restaurant.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${restaurant.cuisineTypes.firstOrNull() ?: ""}, ${restaurant.establishmentTypes.firstOrNull() ?: ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "${restaurant.distanceKm}km",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) DiscountRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CouponCard(
    coupon: Coupon,
    restaurant: Restaurant?,
    onDetails: () -> Unit,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                restaurant?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                // ✅ modo escuro: primaryContainer do tema
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                it.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {},
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Ver restaurante",
                                    color = PrimaryGreen,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    coupon.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    coupon.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    val sdf = java.text.SimpleDateFormat(
                        "dd/MM/yyyy",
                        java.util.Locale("pt", "BR")
                    )
                    Text(
                        "Válido até ${sdf.format(coupon.validUntil)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRedeem,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiscountRed
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Resgatar Cupom",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .background(DiscountRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    coupon.discountValue,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ReservationCard(
    restaurantName: String,
    date: String,
    time: String,
    peopleCount: Int,
    status: ReservationStatus,
    onViewRestaurant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor) = when (status) {
        ReservationStatus.CONFIRMED -> "Confirmada" to StatusOpen
        ReservationStatus.PENDING   -> "Pendente"   to StatusPending
        ReservationStatus.CANCELLED -> "Cancelada"  to StatusClosed
        ReservationStatus.COMPLETED -> "Concluída"  to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    restaurantName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .background(
                            statusColor.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        statusLabel,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        date,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        time,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$peopleCount pessoas",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(
                onClick = onViewRestaurant,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Ver restaurante", color = PrimaryGreen, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    // ✅ modo escuro: primaryContainer do tema
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    review.userName.firstOrNull()?.toString() ?: "U",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    review.userName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (i < review.rating.toInt()) StarYellow
                            else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            val sdf = java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale("pt", "BR")
            )
            Text(
                sdf.format(review.createdAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            review.comment,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (review.restaurantReply.isNotEmpty()) {
            TextButton(
                onClick = {},
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Ver resposta do restaurante",
                    color = PrimaryGreen,
                    fontSize = 13.sp
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun UserBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        data class NavItem(
            val route: String,
            val label: String,
            val icon: androidx.compose.ui.graphics.vector.ImageVector
        )

        val items = listOf(
            NavItem("user_home", "Início", Icons.Default.Home),
            NavItem("search", "Buscar", Icons.Default.Search),
            NavItem("match", "Match", Icons.Default.Star),
            NavItem("coupons", "Cupons", Icons.Default.LocalOffer),
            NavItem("user_profile", "Perfil", Icons.Default.AccountCircle)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        item.label,
                        modifier = Modifier.size(22.dp),
                        tint = if (currentRoute == item.route) PrimaryGreen
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 10.sp,
                        color = if (currentRoute == item.route) PrimaryGreen
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    // ✅ modo escuro: primaryContainer do tema
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun RestaurantBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        data class NavItem(
            val route: String,
            val label: String,
            val icon: androidx.compose.ui.graphics.vector.ImageVector
        )

        val items = listOf(
            NavItem("restaurant_home", "Início", Icons.Default.Home),
            NavItem("restaurant_dashboard", "Dashboard", Icons.Default.BarChart),
            NavItem("restaurant_coupons", "Cupons", Icons.Default.LocalOffer),
            NavItem("restaurant_profile", "Perfil", Icons.Default.AccountCircle)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        item.label,
                        modifier = Modifier.size(22.dp),
                        tint = if (currentRoute == item.route) PrimaryGreen
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 10.sp,
                        color = if (currentRoute == item.route) PrimaryGreen
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}