package com.pentagono.rolematch.presentation.screens.restaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.BorderLight
import com.pentagono.rolematch.core.theme.DarkGreen
import com.pentagono.rolematch.core.theme.DiscountRed
import com.pentagono.rolematch.core.theme.DividerLight
import com.pentagono.rolematch.core.theme.LightGreen
import com.pentagono.rolematch.core.theme.PrimaryGreen
import com.pentagono.rolematch.core.theme.StarYellow
import com.pentagono.rolematch.core.theme.StatusOpen
import com.pentagono.rolematch.core.theme.StatusOpenBg
import com.pentagono.rolematch.core.theme.StatusPending
import com.pentagono.rolematch.core.theme.TextDark
import com.pentagono.rolematch.core.theme.TextLight
import com.pentagono.rolematch.core.theme.TextMuted
import com.pentagono.rolematch.core.theme.WarningYellow
import com.pentagono.rolematch.presentation.components.PrimaryButton
import com.pentagono.rolematch.presentation.components.RestaurantBottomBar
import com.pentagono.rolematch.presentation.components.ReviewCard
import com.pentagono.rolematch.presentation.components.RoleMatchLogo
import com.pentagono.rolematch.presentation.components.SecondaryButton
import com.pentagono.rolematch.presentation.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RestaurantHomeScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPreview: () -> Unit,
    onNavigateToReservations: () -> Unit = {},
    onLogout: () -> Unit,
    restaurantViewModel: RestaurantViewModel = viewModel()
) {
    val restaurantState by restaurantViewModel.state.collectAsState()
    val restaurant = restaurantState.restaurant

    // Reservas reais de hoje do Firestore
    val reservationRepo = remember { com.pentagono.rolematch.data.repository.RepositoryProvider.reservation }
    var reservations by remember { mutableStateOf<List<com.pentagono.rolematch.domain.model.Reservation>>(emptyList()) }

    // Hoje em múltiplos formatos para comparar com o que o usuário digitou
    val todayFormats = remember {
        val cal = java.util.Calendar.getInstance()
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val month = cal.get(java.util.Calendar.MONTH) + 1
        val year = cal.get(java.util.Calendar.YEAR)
        setOf(
            "%02d/%02d/%d".format(day, month, year),   // 02/06/2026
            "%d/%d/%d".format(day, month, year),        // 2/6/2026
            "%02d/%02d/%d".format(day, month, year % 100), // 02/06/26
            "%d/%02d/%d".format(day, month, year)       // 2/06/2026
        )
    }

    LaunchedEffect(restaurant?.id) {
        val restaurantId = restaurant?.id ?: return@LaunchedEffect
        // Busca todas as reservas do restaurante e filtra no cliente por data de hoje
        val result = reservationRepo.getRestaurantReservations(restaurantId, null)
        result.fold(
            onSuccess = { all ->
                reservations = all.filter { it.date.trim() in todayFormats }
            },
            onFailure = {}
        )
    }
    val notifications = listOf(
        Triple("Nova reserva confirmada", "Mesa para 4 pessoas às 20:00", Icons.Default.CheckCircle),
        Triple("Cupom com baixo resgate", "DOCEGRATIS - apenas 5 resgates esta semana", Icons.Default.Warning),
        Triple("Reserva cancelada", "Mesa para 2 pessoas às 19:30", Icons.Default.Cancel)
    )

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RoleMatchLogo()
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Visibility, null, tint = TextMuted)
                    }
                }
            }
        },
        bottomBar = {
            RestaurantBottomBar(currentRoute = "restaurant_home") { route ->
                when (route) {
                    "restaurant_dashboard" -> onNavigateToDashboard()
                    "restaurant_coupons" -> onNavigateToCoupons()
                    "restaurant_profile" -> onNavigateToProfile()
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
                Text("Bem-vindo!", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                Text(restaurant?.name ?: "Carregando...", color = TextMuted, fontSize = 15.sp)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ações Rápidas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickActionButton(Icons.Default.LocalOffer, "Gerenciar Cupons", onNavigateToCoupons, Modifier.weight(1f))
                            QuickActionButton(Icons.Default.Visibility, "Visualizar Perfil", onNavigateToPreview, Modifier.weight(1f))
                            QuickActionButton(Icons.Default.DateRange, "Ver Reservas", onNavigateToReservations, Modifier.weight(1f))
                            QuickActionButton(Icons.Default.BarChart, "Dashboard", onNavigateToDashboard, Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("🔔 Notificações Recentes", "Ver todas")
                        Spacer(modifier = Modifier.height(8.dp))
                        notifications.forEachIndexed { index, notification ->
                            val (title, message, icon) = notification
                            val (backgroundColor, tintColor) = when (index) {
                                0 -> StatusOpenBg to StatusOpen
                                1 -> WarningYellow to StatusPending
                                else -> Color(0xFFFEE2E2) to DiscountRed
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(backgroundColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, null, tint = tintColor, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(message, fontSize = 12.sp, color = TextMuted)
                                }
                            }
                            if (index < notifications.size - 1) HorizontalDivider(color = DividerLight)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("📅 Reservas de Hoje", "${reservations.size} reservas")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (reservations.isEmpty()) {
                            Text("Nenhuma reserva para hoje.", color = TextMuted, fontSize = 14.sp)
                        } else {
                            reservations.forEachIndexed { index, reservation ->
                                val statusColor = when (reservation.status) {
                                    com.pentagono.rolematch.domain.model.ReservationStatus.CONFIRMED -> "Confirmada" to StatusOpen
                                    com.pentagono.rolematch.domain.model.ReservationStatus.PENDING -> "Pendente" to StatusPending
                                    com.pentagono.rolematch.domain.model.ReservationStatus.CANCELLED -> "Cancelada" to DiscountRed
                                    else -> "Concluída" to TextMuted
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(reservation.time, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryGreen, modifier = Modifier.width(52.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(reservation.customerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("${reservation.peopleCount} pessoas", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.second.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(statusColor.first, color = statusColor.second, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (index < reservations.size - 1) HorizontalDivider(color = DividerLight)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🕐 Horário de Funcionamento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Icon(Icons.Default.Edit, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "Segunda-feira" to "18:00 - 23:00",
                            "Terça-feira" to "18:00 - 23:00",
                            "Quarta-feira" to "18:00 - 23:00",
                            "Quinta-feira" to "18:00 - 23:00",
                            "Sexta-feira" to "18:00 - 00:00",
                            "Sábado" to "12:00 - 00:00",
                            "Domingo" to "12:00 - 22:00"
                        ).forEach { (day, time) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(day, fontSize = 13.sp, color = TextMuted)
                                Text(time, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(52.dp).background(LightGreen, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, textAlign = TextAlign.Center, color = TextDark, maxLines = 2)
    }
}

@Composable
fun RestaurantDashboardScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToProfile: () -> Unit,
    restaurantViewModel: RestaurantViewModel = viewModel()
) {
    val restaurantState by restaurantViewModel.state.collectAsState()
    val restaurant = restaurantState.restaurant

    // Métricas reais do Firestore
    val couponRepo = remember { com.pentagono.rolematch.data.repository.RepositoryProvider.coupon }
    val reservationRepo = remember { com.pentagono.rolematch.data.repository.RepositoryProvider.reservation }
    var totalReservations by remember { mutableStateOf("...") }
    var totalCouponsRedeemed by remember { mutableStateOf("...") }
    var avgRating by remember { mutableStateOf("...") }
    var peakData by remember { mutableStateOf<List<Triple<String, String, Int>>>(emptyList()) }

    LaunchedEffect(restaurant?.id) {
        val restaurantId = restaurant?.id ?: return@LaunchedEffect

        // Reservas totais
        val reservResult = reservationRepo.getRestaurantReservations(restaurantId, null)
        reservResult.fold(
            onSuccess = { list ->
                totalReservations = list.size.toString()
                // Calcula pico por dia da semana
                val dayNames = listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR"))
                val countByDay = MutableList(7) { 0 }
                list.forEach { res ->
                    try {
                        val date = sdf.parse(res.date.trim()) ?: return@forEach
                        val cal = java.util.Calendar.getInstance()
                        cal.time = date
                        // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday...7=Saturday
                        val dayIdx = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
                        countByDay[dayIdx]++
                    } catch (e: Exception) {}
                }
                peakData = dayNames.mapIndexed { i, name -> Triple(name, "", countByDay[i]) }
                avgRating = restaurant.rating.let { if (it > 0f) "%.1f".format(it) else "N/A" }
            },
            onFailure = { totalReservations = "?" }
        )

        // Cupons resgatados (soma dos redeemCount)
        val couponResult = couponRepo.getCouponsByRestaurant(restaurantId)
        couponResult.fold(
            onSuccess = { list ->
                totalCouponsRedeemed = list.sumOf { it.redeemCount }.toString()
            },
            onFailure = { totalCouponsRedeemed = "?" }
        )
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoleMatchLogo()
                }
            }
        },
        bottomBar = {
            RestaurantBottomBar(currentRoute = "restaurant_dashboard") { route ->
                when (route) {
                    "restaurant_home" -> onNavigateToHome()
                    "restaurant_coupons" -> onNavigateToCoupons()
                    "restaurant_profile" -> onNavigateToProfile()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Text("", color = TextMuted, fontSize = 14.sp) // dados reais via RestaurantViewModel
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Estimativa de Receita", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                            Text("Através do Rolê Match este mês", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("R$ 12.450", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                            Text("+15% vs mês anterior", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        }
                        Icon(Icons.Default.AttachMoney, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(48.dp))
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("👁️", "—", "Visualizações", "", Modifier.weight(1f))
                    MetricCard("📅", totalReservations, "Reservas", "", Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("⭐", avgRating, "Avaliação", "", Modifier.weight(1f))
                    MetricCard("🎟️", totalCouponsRedeemed, "Cupons Resgatados", "", Modifier.weight(1f))
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Horários de Pico – Reservas por Dia", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Usa dados reais se disponíveis, senão mostra mensagem
                        val displayPeakData = if (peakData.isNotEmpty()) peakData else listOf(
                            Triple("Segunda-feira", "", 0),
                            Triple("Terça-feira", "", 0),
                            Triple("Quarta-feira", "", 0),
                            Triple("Quinta-feira", "", 0),
                            Triple("Sexta-feira", "", 0),
                            Triple("Sábado", "", 0),
                            Triple("Domingo", "", 0)
                        )
                        val maxValue = displayPeakData.maxOf { it.third }.toFloat().coerceAtLeast(1f)

                        displayPeakData.forEach { (day, hours, count) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(day, fontSize = 12.sp, color = TextMuted)
                                    Text(hours, fontSize = 12.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                // ✅ Corrigido: weight nunca é 0f — garante mínimo de 0.01f
                                val barWeight = (count / maxValue).coerceAtLeast(0.01f)
                                val spaceWeight = (1f - barWeight).coerceAtLeast(0.01f)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .weight(barWeight)
                                            .height(22.dp)
                                            .background(PrimaryGreen, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$count reservas",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(spaceWeight))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("⭐ Avaliações Recentes", "Ver todas")
                        Spacer(modifier = Modifier.height(8.dp))
                        MockData.reviews.forEach { review -> ReviewCard(review = review) }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("🎟️ Performance de Cupons", "Gerenciar")
                        Spacer(modifier = Modifier.height(8.dp))
                        MockData.coupons.forEach { coupon ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(coupon.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("${coupon.redeemCount} resgates", fontSize = 12.sp, color = TextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (coupon.active) StatusOpenBg else Color(0xFFF3F4F6),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        if (coupon.active) "Ativo" else "Inativo",
                                        color = if (coupon.active) StatusOpen else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider(color = DividerLight)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PrimaryButton("+ Criar Novo Cupom", onNavigateToCoupons, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(emoji: String, value: String, label: String, trend: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Text(label, fontSize = 12.sp, color = TextMuted)
            if (trend.isNotEmpty()) {
                Text(trend, fontSize = 12.sp, color = StatusOpen, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RestaurantProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToEdit: () -> Unit = {},
    onLogout: () -> Unit,
    restaurantViewModel: RestaurantViewModel = viewModel()
) {
    val restaurantState by restaurantViewModel.state.collectAsState()
    val restaurant = restaurantState.restaurant

    Scaffold(
        bottomBar = {
            RestaurantBottomBar(currentRoute = "restaurant_profile") { route ->
                when (route) {
                    "restaurant_home" -> onNavigateToHome()
                    "restaurant_dashboard" -> onNavigateToDashboard()
                    "restaurant_coupons" -> onNavigateToCoupons()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                        .background(Brush.verticalGradient(listOf(DarkGreen, PrimaryGreen)))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateToHome) {
                            Icon(RMIcons.ArrowBack, "Voltar", tint = Color.White)
                        }
                    }
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                        Box(
                            modifier = Modifier.size(56.dp).background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                        }
                    }
                    Column(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                        Text(restaurant?.name ?: "Carregando...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(restaurant?.phone?.ifEmpty { "Sem telefone" } ?: "", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ProfileSettingsSection(
                    items = listOf(Triple(Icons.Default.Dashboard, "Voltar ao Início") { onNavigateToHome() }),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ProfileSettingsCard(
                    title = "Conta",
                    items = listOf(
                        SettingsItem(Icons.Default.Storefront, "Informações do restaurante", "Editar nome, endereço, contato", onNavigateToEdit),
                        SettingsItem(Icons.Default.Lock, "Alterar senha", "Atualizar senha de acesso")
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notificações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            "Novas reservas" to "Receber notificações de reservas",
                            "Novas avaliações" to "Notificar quando receber reviews",
                            "E-mail" to "Receber relatórios por e-mail"
                        ).forEachIndexed { index, item ->
                            val (label, description) = item
                            var checked by remember { mutableStateOf(index < 2) }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, fontSize = 14.sp)
                                    Text(description, fontSize = 12.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = checked,
                                    onCheckedChange = { checked = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Plano e Assinatura", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Plano Premium", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkGreen)
                                    Text("Recursos ilimitados", fontSize = 13.sp, color = PrimaryGreen)
                                    Text("Próxima renovação: 04/05/2026", fontSize = 12.sp, color = TextMuted)
                                }
                                Icon(Icons.Default.Star, null, tint = StarYellow, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        PrimaryButton("Gerenciar Plano", {}, Modifier.fillMaxWidth())
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ProfileSettingsCard(
                    title = "Ajuda e Suporte",
                    items = listOf(
                        SettingsItem(Icons.Default.Help, "Central de ajuda"),
                        SettingsItem(Icons.Default.ChatBubble, "Fale conosco"),
                        SettingsItem(Icons.Default.Article, "Termos de uso")
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Sair da conta", color = DiscountRed) },
                        leadingContent = { Icon(RMIcons.ExitToApp, null, tint = DiscountRed) },
                        modifier = Modifier.clickable { onLogout() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RoleMatchLogo()
                    Text("Rolê Match para Restaurantes", color = TextMuted, fontSize = 12.sp)
                    Text("Versão 1.0.0", color = TextLight, fontSize = 11.sp)
                }
            }
        }
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val label: String,
    val subtitle: String = "",
    val onClick: () -> Unit = {}
)

@Composable
private fun ProfileSettingsSection(items: List<Triple<ImageVector, String, () -> Unit>>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        items.forEach { (icon, label, onClick) ->
            ListItem(
                headlineContent = { Text(label) },
                leadingContent = { Icon(icon, null, tint = PrimaryGreen) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = TextMuted) },
                modifier = Modifier.clickable { onClick() }
            )
        }
    }
}

@Composable
private fun ProfileSettingsCard(title: String, items: List<SettingsItem>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEachIndexed { index, item ->
                ListItem(
                    headlineContent = { Text(item.label) },
                    supportingContent = if (item.subtitle.isNotEmpty()) {
                        { Text(item.subtitle, color = TextMuted, fontSize = 12.sp) }
                    } else null,
                    leadingContent = { Icon(item.icon, null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = TextMuted) },
                    modifier = Modifier.clickable { item.onClick() }
                )
                if (index < items.size - 1) HorizontalDivider(color = DividerLight)
            }
        }
    }
}