package com.pentagono.rolematch.presentation.screens.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.DarkGreen
import com.pentagono.rolematch.core.theme.DiscountRed
import com.pentagono.rolematch.core.theme.PrimaryGreen
import com.pentagono.rolematch.core.theme.StatusOpen
import com.pentagono.rolematch.core.theme.StatusOpenBg
import com.pentagono.rolematch.core.theme.StatusPending
import com.pentagono.rolematch.core.theme.StatusPendingBg
import com.pentagono.rolematch.core.theme.TextMuted
import com.pentagono.rolematch.presentation.components.PrimaryButton
import com.pentagono.rolematch.presentation.components.SecondaryButton
import com.pentagono.rolematch.presentation.screens.user.RestaurantDetailScreen
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RestaurantPreviewScreen(
    onBack: () -> Unit,
    restaurantViewModel: RestaurantViewModel = viewModel()
) {
    val restaurantState by restaurantViewModel.state.collectAsState()
    val restaurantId = restaurantState.restaurant?.id ?: MockData.currentRestaurant.id

    Box(modifier = Modifier.fillMaxSize()) {
        RestaurantDetailScreen(
            restaurantId = restaurantId,
            onBack = onBack,
            onNavigateToReservation = {},
            onNavigateToCouponDetails = {}
        )

        Surface(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            color = DarkGreen
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(RMIcons.ArrowBack, "Voltar", tint = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Preview do Restaurante", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Como os clientes verão seu perfil", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Edit, "Editar", tint = Color.White)
                }
            }
        }
    }
}

enum class QrValidationState {
    WAITING, SCANNING, VALIDATING, VALID, INVALID, EXPIRED, ALREADY_USED, WRONG_RESTAURANT
}

@Composable
fun RestaurantQrValidatorScreen(onBack: () -> Unit) {
    var validationState by remember { mutableStateOf(QrValidationState.WAITING) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val validatedCoupon = remember { MockData.coupons.firstOrNull() }
    val validatedUser = remember { MockData.currentUser }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(RMIcons.ArrowBack, "Voltar")
                    }
                    Text("Validar Cupom", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (validationState) {
                QrValidationState.WAITING, QrValidationState.SCANNING -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.size(280.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QrCodeScanner, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(80.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Câmera ativa", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                            }
                            val markerColor = PrimaryGreen
                            val markerSize = 40.dp
                            val stroke = 4.dp
                            Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                                Box(modifier = Modifier.size(markerSize).align(Alignment.TopStart).background(Color.Transparent)) {
                                    Box(modifier = Modifier.width(markerSize).height(stroke).background(markerColor).align(Alignment.TopStart))
                                    Box(modifier = Modifier.height(markerSize).width(stroke).background(markerColor).align(Alignment.TopStart))
                                }
                                Box(modifier = Modifier.size(markerSize).align(Alignment.TopEnd)) {
                                    Box(modifier = Modifier.width(markerSize).height(stroke).background(markerColor).align(Alignment.TopEnd))
                                    Box(modifier = Modifier.height(markerSize).width(stroke).background(markerColor).align(Alignment.TopEnd))
                                }
                                Box(modifier = Modifier.size(markerSize).align(Alignment.BottomStart)) {
                                    Box(modifier = Modifier.width(markerSize).height(stroke).background(markerColor).align(Alignment.BottomStart))
                                    Box(modifier = Modifier.height(markerSize).width(stroke).background(markerColor).align(Alignment.BottomStart))
                                }
                                Box(modifier = Modifier.size(markerSize).align(Alignment.BottomEnd)) {
                                    Box(modifier = Modifier.width(markerSize).height(stroke).background(markerColor).align(Alignment.BottomEnd))
                                    Box(modifier = Modifier.height(markerSize).width(stroke).background(markerColor).align(Alignment.BottomEnd))
                                }
                            }
                        }
                    }
                    Text("Aponte a câmera para o QR Code apresentado pelo cliente.", fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton("▶ Simular QR Válido", { validationState = QrValidationState.VALID }, Modifier.fillMaxWidth())
                        SecondaryButton("Simular QR Expirado", { validationState = QrValidationState.EXPIRED }, Modifier.fillMaxWidth())
                        SecondaryButton("Simular QR Já Usado", { validationState = QrValidationState.ALREADY_USED }, Modifier.fillMaxWidth())
                        SecondaryButton("Simular QR Inválido", { validationState = QrValidationState.INVALID }, Modifier.fillMaxWidth())
                    }
                }

                QrValidationState.VALIDATING -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(56.dp))
                    Text("Validando cupom...", color = TextMuted)
                }

                QrValidationState.VALID -> {
                    ValidationResultCard(isSuccess = true, title = "✅ Cupom Válido!", titleColor = StatusOpen, bgColor = StatusOpenBg) {
                        validatedCoupon?.let { coupon ->
                            Text(coupon.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(coupon.description, color = TextMuted, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cliente: ${validatedUser.name}", fontSize = 14.sp)
                            Text("Desconto: ${coupon.discountValue}", fontSize = 14.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            Text("Válido até: ${SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(coupon.validUntil)}", fontSize = 13.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton("✓ Confirmar Uso", { showConfirmDialog = true }, Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        SecondaryButton("Cancelar", { validationState = QrValidationState.WAITING }, Modifier.fillMaxWidth())
                    }
                }

                QrValidationState.EXPIRED -> {
                    ValidationResultCard(isSuccess = false, title = "⏰ Cupom Expirado", titleColor = DiscountRed, bgColor = Color(0xFFFEE2E2)) {
                        Text("Este cupom não é mais válido.", color = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton("Tentar novamente", { validationState = QrValidationState.WAITING }, Modifier.fillMaxWidth())
                    }
                }

                QrValidationState.ALREADY_USED -> {
                    ValidationResultCard(isSuccess = false, title = "🚫 Cupom Já Utilizado", titleColor = StatusPending, bgColor = StatusPendingBg) {
                        Text("Este cupom já foi resgatado anteriormente.", color = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton("Tentar novamente", { validationState = QrValidationState.WAITING }, Modifier.fillMaxWidth())
                    }
                }

                QrValidationState.INVALID, QrValidationState.WRONG_RESTAURANT -> {
                    ValidationResultCard(isSuccess = false, title = "❌ QR Code Inválido", titleColor = DiscountRed, bgColor = Color(0xFFFEE2E2)) {
                        Text(
                            if (validationState == QrValidationState.WRONG_RESTAURANT) "Este cupom pertence a outro restaurante."
                            else "QR Code não reconhecido ou adulterado.",
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton("Tentar novamente", { validationState = QrValidationState.WAITING }, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = StatusOpen, modifier = Modifier.size(40.dp)) },
            title = { Text("Confirmar Uso do Cupom?", fontWeight = FontWeight.Bold) },
            text = { Text("Ao confirmar, o cupom será marcado como utilizado e não poderá ser usado novamente.") },
            confirmButton = {
                Button(
                    onClick = { showConfirmDialog = false; validationState = QrValidationState.WAITING },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Confirmar", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }, shape = RoundedCornerShape(10.dp)) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ValidationResultCard(
    isSuccess: Boolean,
    title: String,
    titleColor: Color,
    bgColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = titleColor)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}