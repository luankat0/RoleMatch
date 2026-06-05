package com.pentagono.rolematch.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.domain.model.RestaurantStatus
import com.pentagono.rolematch.presentation.components.SecondaryButton
import com.pentagono.rolematch.presentation.components.StatusBadge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── Models ───────────────────────────────────────────────────────────────────

data class AdminMetric(val emoji: String, val label: String, val value: String, val sub: String, val color: Color)

data class AdminRestaurantItem(
    val id: String, val name: String, val cuisineTypes: List<String>,
    val status: String, val ownerEmail: String = ""
)

data class AdminUserItem(
    val id: String, val name: String, val email: String,
    val role: String, val isVerified: Boolean
)

data class AdminState(
    val totalUsers: Int = 0,
    val totalRestaurants: Int = 0,
    val pendingRestaurants: Int = 0,
    val totalCoupons: Int = 0,
    val activeCoupons: Int = 0,
    val restaurants: List<AdminRestaurantItem> = emptyList(),
    val users: List<AdminUserItem> = emptyList(),
    val isLoading: Boolean = true
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class AdminViewModel : ViewModel() {
    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()
    private val db = FirebaseFirestore.getInstance()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Users
                val usersSnap = db.collection("users").get().await()
                val users = usersSnap.documents.mapNotNull { doc ->
                    try {
                        AdminUserItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            role = doc.getString("role") ?: "USER",
                            isVerified = doc.getBoolean("isVerified") ?: false
                        )
                    } catch (e: Exception) { null }
                }

                // Restaurants
                val restSnap = db.collection("restaurants").get().await()
                val restaurants = restSnap.documents.mapNotNull { doc ->
                    try {
                        AdminRestaurantItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            cuisineTypes = (doc.get("cuisineTypes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            status = doc.getString("status") ?: "PENDING_REVIEW"
                        )
                    } catch (e: Exception) { null }
                }

                // Coupons
                val couponSnap = db.collection("coupons").get().await()
                val totalCoupons = couponSnap.size()
                val activeCoupons = couponSnap.documents.count { it.getBoolean("active") == true }

                _state.value = AdminState(
                    totalUsers = users.size,
                    totalRestaurants = restaurants.size,
                    pendingRestaurants = restaurants.count { it.status == "PENDING_REVIEW" },
                    totalCoupons = totalCoupons,
                    activeCoupons = activeCoupons,
                    restaurants = restaurants,
                    users = users,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun approveRestaurant(restaurantId: String) {
        viewModelScope.launch {
            db.collection("restaurants").document(restaurantId)
                .update("status", RestaurantStatus.ACTIVE.name).await()
            loadAll()
        }
    }

    fun rejectRestaurant(restaurantId: String) {
        viewModelScope.launch {
            db.collection("restaurants").document(restaurantId)
                .update("status", RestaurantStatus.REJECTED.name).await()
            loadAll()
        }
    }

    fun suspendRestaurant(restaurantId: String) {
        viewModelScope.launch {
            db.collection("restaurants").document(restaurantId)
                .update("status", RestaurantStatus.SUSPENDED.name).await()
            loadAll()
        }
    }
}

// ─── AdminDashboardScreen ─────────────────────────────────────────────────────

@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit,
    onNavigateToRestaurants: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToReports: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Surface(color = DarkGreen, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ROLÊ MATCH", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Painel Administrativo", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(RMIcons.ExitToApp, null, tint = Color.White)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Text("Visão geral da plataforma", color = TextMuted, fontSize = 14.sp)
            }

            item {
                val metrics = listOf(
                    AdminMetric("👥", "Usuários", "${state.totalUsers}", "cadastrados", PrimaryGreen),
                    AdminMetric("🏪", "Restaurantes", "${state.totalRestaurants}", "cadastrados", BlueAccent),
                    AdminMetric("⏳", "Pendentes", "${state.pendingRestaurants}", "aguardando", StatusPending),
                    AdminMetric("🎟️", "Cupons Ativos", "${state.activeCoupons}", "de ${state.totalCoupons} total", OrangeAccent)
                )
                val rows = metrics.chunked(2)
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { metric ->
                            AdminMetricCard(metric, Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            item { Text("Gerenciamento", fontWeight = FontWeight.Bold, fontSize = 18.sp) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminNavCard("👥 Usuários", "Gerenciar contas", onNavigateToUsers, Modifier.weight(1f))
                    AdminNavCard("🏪 Restaurantes", "Aprovar e gerenciar", onNavigateToRestaurants, Modifier.weight(1f))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminNavCard("🎟️ Cupons", "Todos os cupons", onNavigateToCoupons, Modifier.weight(1f))
                    AdminNavCard("📋 Relatórios", "Logs e denúncias", onNavigateToReports, Modifier.weight(1f))
                }
            }

            // Restaurantes pendentes
            val pending = state.restaurants.filter { it.status == "PENDING_REVIEW" }
            if (pending.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("⏳ Restaurantes Pendentes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                TextButton(onClick = onNavigateToRestaurants) {
                                    Text("Ver todos", color = PrimaryGreen, fontSize = 13.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            pending.take(3).forEach { restaurant ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(restaurant.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("Aguardando aprovação", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = { viewModel.approveRestaurant(restaurant.id) },
                                            modifier = Modifier.size(32.dp).background(StatusOpenBg, RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Default.Check, null, tint = StatusOpen, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.rejectRestaurant(restaurant.id) },
                                            modifier = Modifier.size(32.dp).background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = DiscountRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                HorizontalDivider(color = DividerLight)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── AdminRestaurantsScreen ───────────────────────────────────────────────────

@Composable
fun AdminRestaurantsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var filterStatus by remember { mutableStateOf("Todos") }

    val filtered = when (filterStatus) {
        "Pendentes" -> state.restaurants.filter { it.status == "PENDING_REVIEW" }
        "Ativos" -> state.restaurants.filter { it.status == "ACTIVE" }
        "Suspensos" -> state.restaurants.filter { it.status == "SUSPENDED" || it.status == "REJECTED" }
        else -> state.restaurants
    }

    Scaffold(
        topBar = { AdminSimpleTopBar("Restaurantes", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Pendentes", "Ativos", "Suspensos").forEach { f ->
                    FilterChip(
                        selected = filterStatus == f,
                        onClick = { filterStatus = f },
                        label = { Text(f, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
                return@Scaffold
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("${filtered.size} restaurantes", color = TextMuted, fontSize = 14.sp)
                }
                items(filtered, key = { it.id }) { restaurant ->
                    val (statusColor, statusLabel) = when (restaurant.status) {
                        "ACTIVE" -> StatusOpen to "Ativo"
                        "PENDING_REVIEW" -> StatusPending to "Pendente"
                        "SUSPENDED" -> DiscountRed to "Suspenso"
                        "REJECTED" -> DiscountRed to "Rejeitado"
                        else -> TextMuted to restaurant.status
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(44.dp).background(LightGreen, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Restaurant, null, tint = PrimaryGreen)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(restaurant.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(restaurant.cuisineTypes.joinToString(", ").ifEmpty { "Sem categoria" }, fontSize = 12.sp, color = TextMuted)
                                }
                                Box(
                                    modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(statusLabel, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (restaurant.status == "PENDING_REVIEW") {
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.approveRestaurant(restaurant.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Aprovar", fontSize = 13.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.rejectRestaurant(restaurant.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DiscountRed),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Rejeitar", fontSize = 13.sp, color = DiscountRed)
                                    }
                                }
                            } else if (restaurant.status == "ACTIVE") {
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.suspendRestaurant(restaurant.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DiscountRed),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Suspender", fontSize = 13.sp, color = DiscountRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── AdminUsersScreen ─────────────────────────────────────────────────────────

@Composable
fun AdminUsersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { AdminSimpleTopBar("Usuários", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("${state.users.size} usuários cadastrados", color = TextMuted, fontSize = 14.sp) }

            items(state.users, key = { it.id }) { user ->
                val roleColor = when (user.role) {
                    "ADMIN" -> DiscountRed
                    "RESTAURANT" -> BlueAccent
                    else -> PrimaryGreen
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).background(LightGreen, RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.name.firstOrNull()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name.ifEmpty { "Sem nome" }, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(user.email, fontSize = 12.sp, color = TextMuted)
                        }
                        Box(
                            modifier = Modifier.background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(user.role, fontSize = 11.sp, color = roleColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─── AdminCouponsScreen ───────────────────────────────────────────────────────

@Composable
fun AdminCouponsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var allCoupons by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snap = FirebaseFirestore.getInstance().collection("coupons").get().await()
            allCoupons = snap.documents.mapNotNull { doc ->
                mapOf(
                    "id" to (doc.id),
                    "title" to (doc.getString("title") ?: ""),
                    "restaurantId" to (doc.getString("restaurantId") ?: ""),
                    "discountValue" to (doc.getString("discountValue") ?: ""),
                    "redeemCount" to (doc.getLong("redeemCount") ?: 0L),
                    "active" to (doc.getBoolean("active") ?: true)
                )
            }
        } catch (e: Exception) { }
        isLoading = false
    }

    Scaffold(
        topBar = { AdminSimpleTopBar("Cupons", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("${allCoupons.size} cupons cadastrados", color = TextMuted, fontSize = 14.sp) }
            items(allCoupons) { coupon ->
                val active = coupon["active"] as? Boolean ?: true
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).background(if (active) StatusOpenBg else Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalOffer, null, tint = if (active) StatusOpen else TextMuted)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coupon["title"] as? String ?: "", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${coupon["redeemCount"]} resgates • ${coupon["discountValue"]}", fontSize = 11.sp, color = PrimaryGreen)
                        }
                        Box(
                            modifier = Modifier.background(if (active) StatusOpenBg else Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(if (active) "Ativo" else "Inativo", fontSize = 12.sp, color = if (active) StatusOpen else TextMuted, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─── AdminReportsScreen ───────────────────────────────────────────────────────

@Composable
fun AdminReportsScreen(onBack: () -> Unit) {
    val reports = listOf(
        Triple("Conteúdo inapropriado", "Avaliação do restaurante Burger & Co.", "Aberta"),
        Triple("Cupom inválido", "Usuário relatou cupom que não foi aceito.", "Em análise"),
        Triple("Perfil falso", "Suspeita de conta duplicada.", "Resolvida")
    )

    Scaffold(
        topBar = { AdminSimpleTopBar("Relatórios", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("${reports.size} denúncias registradas", color = TextMuted, fontSize = 14.sp) }
            items(reports) { (title, desc, status) ->
                val (statusColor, statusBg) = when (status) {
                    "Aberta" -> DiscountRed to Color(0xFFFEE2E2)
                    "Em análise" -> StatusPending to StatusPendingBg
                    else -> StatusOpen to StatusOpenBg
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Flag, null, tint = DiscountRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(desc, fontSize = 12.sp, color = TextMuted)
                        }
                        Box(
                            modifier = Modifier.background(statusBg, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(status, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun AdminMetricCard(metric: AdminMetric, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(metric.emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(metric.value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = metric.color)
            Text(metric.label, fontSize = 12.sp, color = TextMuted)
            Text(metric.sub, fontSize = 11.sp, color = metric.color.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun AdminNavCard(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            Icon(Icons.Default.ArrowForward, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AdminSimpleTopBar(title: String, onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(RMIcons.ArrowBack, "Voltar") }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}