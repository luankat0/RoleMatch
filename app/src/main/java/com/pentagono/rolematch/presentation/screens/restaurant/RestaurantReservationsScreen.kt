package com.pentagono.rolematch.presentation.screens.restaurant

import androidx.compose.foundation.background
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.di.appViewModel
import com.pentagono.rolematch.domain.model.Reservation
import com.pentagono.rolematch.domain.model.ReservationStatus
import com.pentagono.rolematch.domain.repository.ReservationRepository
import com.pentagono.rolematch.presentation.components.SecondaryButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class RestaurantReservationsState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String = "",
    val filterTab: String = "todas" // "todas", "hoje", "confirmadas", "pendentes"
)

class RestaurantReservationsViewModel @Inject constructor(
    private val reservationRepo: ReservationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RestaurantReservationsState())
    val state: StateFlow<RestaurantReservationsState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var allReservations: List<Reservation> = emptyList()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val query = firestore.collection("restaurants")
                    .whereEqualTo("ownerId", uid).limit(1).get().await()
                val restaurantId = query.documents.firstOrNull()?.id ?: return@launch

                val result = reservationRepo.getRestaurantReservations(restaurantId, null)
                result.fold(
                    onSuccess = { list ->
                        allReservations = list
                        applyFilter(_state.value.filterTab)
                        _state.value = _state.value.copy(isLoading = false)
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar")
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Erro")
            }
        }
    }

    fun setFilter(tab: String) {
        _state.value = _state.value.copy(filterTab = tab)
        applyFilter(tab)
    }

    private fun applyFilter(tab: String) {
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR"))
            .format(java.util.Date())
        val filtered = when (tab) {
            "hoje" -> allReservations.filter { it.date.trim() == today }
            "confirmadas" -> allReservations.filter { it.status == ReservationStatus.CONFIRMED }
            "pendentes" -> allReservations.filter { it.status == ReservationStatus.PENDING }
            else -> allReservations
        }
        _state.value = _state.value.copy(reservations = filtered)
    }
}

@Composable
fun RestaurantReservationsScreen(
    onBack: () -> Unit,
    viewModel: RestaurantReservationsViewModel = appViewModel { RestaurantReservationsViewModel(it.reservationRepository()) }
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
                        Icon(RMIcons.ArrowBack, "Voltar")
                    }
                    Column {
                        Text("Reservas", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("${state.reservations.size} reservas encontradas", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("todas" to "Todas", "hoje" to "Hoje", "confirmadas" to "Confirmadas", "pendentes" to "Pendentes")
                    .forEach { (key, label) ->
                        val selected = state.filterTab == key
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setFilter(key) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
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

            if (state.error.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error, color = DiscountRed, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        SecondaryButton("Tentar novamente", viewModel::load)
                    }
                }
                return@Scaffold
            }

            if (state.reservations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DateRange, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Nenhuma reserva encontrada", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("As reservas aparecerão aqui", color = TextMuted, fontSize = 13.sp)
                    }
                }
                return@Scaffold
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.reservations, key = { it.id }) { reservation ->
                    ReservationItemCard(reservation)
                }
            }
        }
    }
}

@Composable
private fun ReservationItemCard(reservation: Reservation) {
    val (statusLabel, statusColor) = when (reservation.status) {
        ReservationStatus.CONFIRMED -> "Confirmada" to StatusOpen
        ReservationStatus.PENDING -> "Pendente" to StatusPending
        ReservationStatus.CANCELLED -> "Cancelada" to DiscountRed
        else -> "Concluída" to TextMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Horário
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Text(reservation.time, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryGreen)
                Text(reservation.date, fontSize = 10.sp, color = TextMuted)
            }

            Spacer(Modifier.width(12.dp))

            // Dados
            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.customerName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${reservation.peopleCount} pessoas", fontSize = 13.sp, color = TextMuted)
                }
                if (reservation.phone.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(reservation.phone, fontSize = 12.sp, color = TextMuted)
                    }
                }
                if (reservation.notes.isNotEmpty()) {
                    Text("📝 ${reservation.notes}", fontSize = 12.sp, color = TextMuted, maxLines = 1)
                }
            }

            // Status
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}