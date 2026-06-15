package com.pentagono.rolematch.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.common.QrCodeGenerator
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.BorderLight
import com.pentagono.rolematch.core.theme.DiscountRed
import com.pentagono.rolematch.core.theme.DividerLight
import com.pentagono.rolematch.core.theme.LightGreen
import com.pentagono.rolematch.core.theme.PrimaryGreen
import com.pentagono.rolematch.core.theme.StarYellow
import com.pentagono.rolematch.core.theme.StatusOpen
import com.pentagono.rolematch.core.theme.StatusOpenBg
import com.pentagono.rolematch.core.theme.TextDark
import com.pentagono.rolematch.core.theme.TextLight
import com.pentagono.rolematch.core.theme.TextMuted
import com.pentagono.rolematch.core.theme.ThemeMode
import com.pentagono.rolematch.core.theme.ThemeViewModel
import com.pentagono.rolematch.domain.model.MatchResult
import com.pentagono.rolematch.presentation.components.PrimaryButton
import com.pentagono.rolematch.presentation.components.RoleMatchLogo
import com.pentagono.rolematch.presentation.components.RoleMatchTextField
import com.pentagono.rolematch.presentation.components.SecondaryButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// VerifyCodeScreen

@Composable
fun VerifyCodeScreen(
    accountType: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .statusBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        RoleMatchLogo()
        Spacer(modifier = Modifier.height(40.dp))
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = RMIcons.Email, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Confirme seu e-mail", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Enviamos um código de verificação para o seu e-mail. Digite-o abaixo.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(28.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.filter { char -> char.isDigit() } },
                    label = { Text("Código de 6 dígitos") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); Text(text = error, color = DiscountRed, fontSize = 13.sp) }
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(text = "Verificar", onClick = { if (code.length < 6) { error = "Digite o código completo"; return@PrimaryButton }; isLoading = true; onVerified() }, isLoading = isLoading, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = {}) { Text(text = "Reenviar código", color = PrimaryGreen) }
                TextButton(onClick = onBack) { Text(text = "Voltar", color = TextMuted) }
            }
        }
    }
}

// ReservationScreen

@Composable
fun ReservationScreen(restaurantId: String, onBack: () -> Unit, onConfirmed: () -> Unit) {
    var restaurant by remember { mutableStateOf(MockData.getRestaurantById(restaurantId)) }
    var restaurantName by remember { mutableStateOf(restaurant?.name ?: "") }

    LaunchedEffect(restaurantId) {
        if (restaurant == null) {
            try {
                val doc = FirebaseFirestore.getInstance().collection("restaurants").document(restaurantId).get().await()
                restaurantName = doc.getString("name") ?: "Restaurante"
            } catch (e: Exception) { restaurantName = "Restaurante" }
        } else { restaurantName = restaurant!!.name }
    }

    var selectedTime by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var peopleCount by remember { mutableStateOf("") }
    var occasionType by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val reservationContext = androidx.compose.ui.platform.LocalContext.current
    val reservationRepo = remember {
        (reservationContext.applicationContext as com.pentagono.rolematch.RoleMatchApplication).appComponent.reservationRepository()
    }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val timeSlots = listOf("12:00","12:30","13:00","13:30","14:00","14:30","18:00","18:30","19:00","19:30","20:00","20:30","21:00","21:30","22:00")

    if (showSuccess) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.padding(24.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusOpen, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Reserva Confirmada!", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(text = "Sua reserva em $restaurantName foi realizada com sucesso.", color = TextMuted, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(text = "Voltar", onClick = onConfirmed, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar") }
                    Column { Text(text = "Fazer Reserva", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(text = restaurantName, fontSize = 13.sp, color = TextMuted) }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (errorMsg.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    PrimaryButton(
                        text = "Confirmar Reserva",
                        onClick = {
                            if (selectedDate.isBlank() || selectedTime.isBlank()) { errorMsg = "Selecione data e horário"; return@PrimaryButton }
                            if (name.isBlank()) { errorMsg = "Informe seu nome"; return@PrimaryButton }
                            isLoading = true; errorMsg = ""
                            scope.launch {
                                val reservation = com.pentagono.rolematch.domain.model.Reservation(
                                    restaurantId = restaurantId, restaurantName = restaurantName,
                                    date = selectedDate, time = selectedTime,
                                    peopleCount = peopleCount.toIntOrNull() ?: 2,
                                    occasionType = occasionType, customerName = name,
                                    phone = phone, email = email, notes = notes
                                )
                                val result = reservationRepo.createReservation(reservation)
                                isLoading = false
                                result.fold(onSuccess = { showSuccess = true }, onFailure = { e -> errorMsg = e.message ?: "Erro ao fazer reserva" })
                            }
                        },
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Data da reserva", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = selectedDate, onValueChange = { selectedDate = it }, placeholder = { Text("dd/mm/aaaa") }, leadingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = TextMuted) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight), modifier = Modifier.fillMaxWidth())
                }
            }
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Horário", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    timeSlots.chunked(4).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { time ->
                                val isSelected = selectedTime == time
                                androidx.compose.material3.Button(onClick = { selectedTime = time }, colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp), modifier = Modifier.weight(1f)) { Text(text = time, fontSize = 13.sp) }
                            }
                            repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(value = peopleCount, onValueChange = { peopleCount = it }, label = { Text("Número de pessoas") }, leadingIcon = { Icon(Icons.Default.Group, null, tint = TextMuted) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = occasionType, onValueChange = { occasionType = it }, label = { Text("Tipo de rolê") }, leadingIcon = { Icon(Icons.Default.Celebration, null, tint = TextMuted) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome completo") }, leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = TextMuted) }, placeholder = { Text("Seu nome") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefone") }, leadingIcon = { Icon(Icons.Default.Call, null, tint = TextMuted) }, placeholder = { Text("(11) 98765-4321") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") }, leadingIcon = { Icon(RMIcons.Email, null, tint = TextMuted) }, placeholder = { Text("seu@email.com") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Observações (opcional)") }, leadingIcon = { Icon(Icons.Default.Notes, null, tint = TextMuted) }, placeholder = { Text("Alguma preferência? Mesa longe da cozinha...") }, minLines = 3, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, unfocusedBorderColor = BorderLight), modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// CouponDetailsScreen

@Composable
fun CouponDetailsScreen(couponId: String, onBack: () -> Unit, onNavigateToQr: (String) -> Unit, onNavigateToRestaurant: (String) -> Unit) {
    var coupon by remember { mutableStateOf(MockData.coupons.find { it.id == couponId }) }
    var restaurantName by remember { mutableStateOf("") }
    var restaurantId by remember { mutableStateOf("") }
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR"))

    LaunchedEffect(couponId) {
        if (coupon == null) {
            try {
                val doc = FirebaseFirestore.getInstance().collection("coupons").document(couponId).get().await()
                if (doc.exists()) {
                    coupon = com.pentagono.rolematch.domain.model.Coupon(id = doc.id, restaurantId = doc.getString("restaurantId") ?: "", title = doc.getString("title") ?: "", description = doc.getString("description") ?: "", benefitType = doc.getString("benefitType") ?: "PERCENTAGE", discountValue = doc.getString("discountValue") ?: "", rules = doc.getString("rules") ?: "", validFrom = doc.getTimestamp("validFrom")?.toDate() ?: java.util.Date(), validUntil = doc.getTimestamp("validUntil")?.toDate() ?: java.util.Date(), maxUses = (doc.getLong("maxUses") ?: 100L).toInt(), active = doc.getBoolean("active") ?: true, redeemCount = (doc.getLong("redeemCount") ?: 0L).toInt(), internalCode = doc.getString("internalCode") ?: "")
                }
            } catch (e: Exception) { }
        }
        val rid = coupon?.restaurantId ?: ""; restaurantId = rid
        val mockR = MockData.getRestaurantById(rid)
        if (mockR != null) { restaurantName = mockR.name } else if (rid.isNotEmpty()) {
            try { val rdoc = FirebaseFirestore.getInstance().collection("restaurants").document(rid).get().await(); restaurantName = rdoc.getString("name") ?: "" } catch (e: Exception) { }
        }
    }

    val c = coupon ?: return

    Scaffold(
        topBar = { Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) { Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar") }; Text(text = "Cupons & Promoções", fontWeight = FontWeight.Bold, fontSize = 18.sp) } } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp)) {
                Row(modifier = Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                    Column(modifier = Modifier.weight(1f).padding(20.dp)) {
                        if (restaurantName.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).background(LightGreen, RoundedCornerShape(10.dp)))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column { Text(text = restaurantName, fontWeight = FontWeight.Bold, fontSize = 15.sp); TextButton(onClick = { onNavigateToRestaurant(restaurantId) }, contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.LocationOn, null, tint = PrimaryGreen, modifier = Modifier.size(14.dp)); Text("Ver restaurante", color = PrimaryGreen, fontSize = 12.sp) } }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerLight)
                        }
                        Text(text = c.title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Text(text = c.description, color = TextMuted, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DateRange, null, tint = TextMuted, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Válido até ${sdf.format(c.validUntil)}", fontSize = 13.sp, color = TextMuted) }
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Button(onClick = { onNavigateToQr("uc_${c.id}") }, colors = ButtonDefaults.buttonColors(containerColor = DiscountRed), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) { Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(text = "Gerar QR Code", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                    Box(modifier = Modifier.width(64.dp).fillMaxSize().background(DiscountRed, RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)), contentAlignment = Alignment.Center) { Text(text = c.discountValue, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, textAlign = TextAlign.Center) }
                }
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "Regras", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    RuleSection("Validade", "Este cupom é válido até ${sdf.format(c.validUntil)}. Após esta data, o cupom não poderá mais ser utilizado.")
                    RuleSection("Como usar", "Para resgatar este cupom, toque em 'Gerar QR Code' e apresente o QR Code ao garçom ou no caixa do restaurante antes de solicitar a conta.")
                    RuleSection("Restrições", "• Válido apenas para consumo no local\n• Não cumulativo com outras promoções\n• Um cupom por pessoa/mesa\n• Não válido em feriados\n• Sujeito a disponibilidade de lugares")
                    RuleSection("Itens excluídos", "O desconto não se aplica a bebidas alcoólicas importadas, pratos especiais do chef e serviços de taxa de entrega.")
                    RuleSection("Cancelamento", "O restaurante se reserva o direito de cancelar este cupom a qualquer momento por motivos operacionais.")
                    HorizontalDivider(color = DividerLight)
                    Text(text = "Ao resgatar este cupom, você concorda com os termos e condições descritos acima.", fontSize = 12.sp, color = TextLight, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun RuleSection(title: String, text: String) {
    Column { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp); Spacer(modifier = Modifier.height(4.dp)); Text(text = text, fontSize = 13.sp, color = TextMuted) }
}

// CouponQrCodeScreen

@Composable
fun CouponQrCodeScreen(userCouponId: String, onBack: () -> Unit) {
    val couponId = userCouponId.removePrefix("uc_")
    var coupon by remember { mutableStateOf(MockData.coupons.find { it.id == couponId }) }
    var restaurantName by remember { mutableStateOf("") }
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR"))

    LaunchedEffect(couponId) {
        if (coupon == null) {
            try {
                val doc = FirebaseFirestore.getInstance().collection("coupons").document(couponId).get().await()
                if (doc.exists()) { coupon = com.pentagono.rolematch.domain.model.Coupon(id = doc.id, restaurantId = doc.getString("restaurantId") ?: "", title = doc.getString("title") ?: "", description = doc.getString("description") ?: "", benefitType = doc.getString("benefitType") ?: "PERCENTAGE", discountValue = doc.getString("discountValue") ?: "", validFrom = doc.getTimestamp("validFrom")?.toDate() ?: java.util.Date(), validUntil = doc.getTimestamp("validUntil")?.toDate() ?: java.util.Date(), internalCode = doc.getString("internalCode") ?: "") }
            } catch (e: Exception) { }
        }
        val rid = coupon?.restaurantId ?: ""
        val mockR = MockData.getRestaurantById(rid)
        if (mockR != null) { restaurantName = mockR.name } else if (rid.isNotEmpty()) {
            try { val rdoc = FirebaseFirestore.getInstance().collection("restaurants").document(rid).get().await(); restaurantName = rdoc.getString("name") ?: "" } catch (e: Exception) { }
        }
    }

    val c = coupon ?: MockData.coupons.firstOrNull() ?: return
    val qrContent = "ROLEMATCH_COUPON|id:${c.id}|restaurant:${c.restaurantId}|discount:${c.discountValue}|valid:${sdf.format(c.validUntil)}|user_coupon:$userCouponId"
    val qrBitmap = remember(qrContent) { QrCodeGenerator.generate(qrContent) }

    Scaffold(
        topBar = { Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) { Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar") }; Text(text = "Resgatar Cupom", fontWeight = FontWeight.Bold, fontSize = 18.sp) } } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (restaurantName.isNotEmpty()) Text(text = restaurantName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = c.title, color = TextMuted, fontSize = 15.sp, textAlign = TextAlign.Center)
            Box(modifier = Modifier.background(StatusOpenBg, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 6.dp)) { Text(text = "✓ Disponível", color = StatusOpen, fontWeight = FontWeight.Bold) }
            Card(modifier = Modifier.size(260.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(6.dp)) {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(bitmap = qrBitmap, contentDescription = "QR Code do cupom", modifier = Modifier.fillMaxSize())
                }
            }
            Box(modifier = Modifier.background(DiscountRed, RoundedCornerShape(12.dp)).padding(horizontal = 20.dp, vertical = 8.dp)) { Text(text = c.discountValue, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp) }
            Text(text = "Apresente este QR Code ao garçom ou no caixa para validar seu desconto.", fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center)
            Text(text = "Válido até ${sdf.format(c.validUntil)}", fontSize = 13.sp, color = TextMuted)
            SecondaryButton(text = "Fechar", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

// MatchResultsScreen

@Composable
fun MatchResultsScreen(
    onBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    viewModel: MatchViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val results = state.results

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar")
                    }
                    Column {
                        Text("Seus Matches! 🎉", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "${results.size} restaurantes encontrados",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = PrimaryGreen)
                    Text("Buscando o seu Match perfeito...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            return@Scaffold
        }

        if (state.error.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("Erro ao buscar restaurantes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(state.error, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    SecondaryButton(text = "Tentar novamente", onClick = onBack)
                }
            }
            return@Scaffold
        }

        if (results.isEmpty() && state.hasSearched) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("Nenhum restaurante encontrado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Tente ajustar os filtros para ver mais opções", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    SecondaryButton(text = "Voltar e ajustar", onClick = onBack)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = results, key = { it.restaurant.id }) { matchResult ->
                MatchResultCard(matchResult = matchResult, onClick = { onNavigateToRestaurant(matchResult.restaurant.id) })
            }
        }
    }
}

@Composable
private fun MatchResultCard(matchResult: MatchResult, onClick: () -> Unit) {
    val restaurant = matchResult.restaurant
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    if (restaurant.cuisineTypes.isNotEmpty()) {
                        Text(restaurant.cuisineTypes.take(2).joinToString(" · "), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(modifier = Modifier.background(PrimaryGreen, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("${matchResult.score}pts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (restaurant.rating > 0f) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("⭐", fontSize = 13.sp)
                        Text("${restaurant.rating}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("(${restaurant.reviewCount})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (restaurant.distanceKm > 0f) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text("${"%.1f".format(restaurant.distanceKm)}km", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("$".repeat(restaurant.priceRange.coerceIn(1, 4)), fontSize = 13.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
            if (matchResult.matchReasons.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    matchResult.matchReasons.take(3).forEach { reason ->
                        Box(modifier = Modifier.background(LightGreen, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text(reason, fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ─── UserSettingsScreen ───────────────────────────────────────────────────────

@Composable
fun UserSettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel? = null,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToContact: () -> Unit = {}
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(true) }
    val currentThemeFlow = themeViewModel?.themeMode
    val currentTheme: ThemeMode = if (currentThemeFlow != null) currentThemeFlow.collectAsStateWithLifecycle().value else ThemeMode.SYSTEM

    Scaffold(
        topBar = { Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) { Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar") }; Text(text = "Configurações", fontWeight = FontWeight.Bold, fontSize = 20.sp) } } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsGroupCard(title = "Notificações") {
                SettingsToggleRow(icon = Icons.Default.Notifications, label = "Push notifications", subtitle = "Receber notificações do app", checked = pushEnabled) { pushEnabled = it }
                HorizontalDivider(color = DividerLight)
                SettingsToggleRow(icon = RMIcons.Email, label = "E-mail", subtitle = "Receber ofertas por e-mail", checked = emailEnabled) { emailEnabled = it }
            }
            SettingsGroupCard(title = "Aparência") {
                SettingsNavRow(icon = Icons.Default.Language, label = "Idioma", subtitle = "Português (Brasil)")
                HorizontalDivider(color = DividerLight)
                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(14.dp)); Text(text = "Aparência", modifier = Modifier.weight(1f)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                            val selected = currentTheme == mode
                            FilterChip(selected = selected, onClick = { themeViewModel?.setTheme(mode) }, label = { Text(text = mode.label, fontSize = 12.sp) }, shape = RoundedCornerShape(20.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White), border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected, borderColor = BorderLight, selectedBorderColor = PrimaryGreen), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            SettingsGroupCard(title = "Privacidade e Segurança") {
                SettingsNavRow(icon = Icons.Default.Lock, label = "Alterar senha", onClick = onNavigateToChangePassword)
                HorizontalDivider(color = DividerLight)
                SettingsNavRow(icon = Icons.Default.Security, label = "Privacidade", subtitle = "Controle seus dados", onClick = onNavigateToPrivacy)
            }
            SettingsGroupCard(title = "Ajuda e Suporte") {
                SettingsNavRow(icon = Icons.Default.Help, label = "Central de ajuda", onClick = onNavigateToHelpCenter)
                HorizontalDivider(color = DividerLight)
                SettingsNavRow(icon = Icons.Default.ChatBubble, label = "Fale conosco", onClick = onNavigateToContact)
                HorizontalDivider(color = DividerLight)
                SettingsNavRow(icon = Icons.Default.Article, label = "Termos de uso", onClick = onNavigateToTerms)
                HorizontalDivider(color = DividerLight)
                SettingsNavRow(icon = Icons.Default.Security, label = "Política de privacidade", onClick = onNavigateToPrivacy)
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { RoleMatchLogo(); Spacer(modifier = Modifier.height(4.dp)); Text(text = "Versão 1.0.0", color = TextLight, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp); Spacer(modifier = Modifier.height(8.dp)); content() }
    }
}

@Composable
private fun SettingsToggleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    ListItem(headlineContent = { Text(text = label) }, supportingContent = { Text(text = subtitle, color = TextMuted, fontSize = 12.sp) }, leadingContent = { Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)) }, trailingContent = { Switch(checked = checked, onCheckedChange = onChecked, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)) })
}

@Composable
private fun SettingsNavRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, subtitle: String = "", onClick: () -> Unit = {}) {
    ListItem(headlineContent = { Text(text = label) }, supportingContent = if (subtitle.isNotEmpty()) { { Text(text = subtitle, color = TextMuted, fontSize = 12.sp) } } else null, leadingContent = { Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)) }, trailingContent = { Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted) }, modifier = Modifier.clickable { onClick() })
}

// ─── NotificationsScreen ──────────────────────────────────────────────────────

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notifications = listOf(Triple("Novo cupom disponível", "Trattoria Bella Vista lançou 20% OFF no jantar!", "2 min atrás"), Triple("Cupom perto de expirar", "Seu cupom Sobremesa grátis expira em 3 dias.", "1 hora atrás"), Triple("Reserva confirmada", "Sua reserva no Sushi House Premium foi confirmada.", "Ontem"))

    Scaffold(
        topBar = { Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) { Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar") }; Text(text = "Notificações", fontWeight = FontWeight.Bold, fontSize = 20.sp) } } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            notifications.forEach { (title, body, time) ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(40.dp).background(LightGreen, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) { Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text(text = body, fontSize = 13.sp, color = TextMuted) }
                        Text(text = time, fontSize = 11.sp, color = TextLight)
                    }
                }
            }
        }
    }
}