package com.pentagono.rolematch.presentation.screens.restaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.theme.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.pentagono.rolematch.core.theme.BorderLight
import com.pentagono.rolematch.core.theme.StatusOpen
import com.pentagono.rolematch.core.theme.StatusOpenBg
import com.pentagono.rolematch.core.theme.TextDark
import com.pentagono.rolematch.core.theme.TextLight
import com.pentagono.rolematch.core.theme.WarningYellow
import com.pentagono.rolematch.di.appViewModel
import com.pentagono.rolematch.domain.model.Coupon
import com.pentagono.rolematch.domain.repository.CouponRepository
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────────────────────────

data class CouponsUiState(
    val coupons: List<Coupon> = emptyList(),
    val isLoading: Boolean = true,
    val error: String = "",
    val restaurantId: String = ""
)

data class CouponFormState(
    val title: String = "",
    val description: String = "",
    val benefitType: String = "PERCENTAGE",
    val discountValue: String = "",
    val internalCode: String = "",
    val rules: String = "",
    val validUntilStr: String = "",
    val maxUses: String = "100",
    val isLoading: Boolean = false,
    val error: String = "",
    val titleError: String = "",
    val discountError: String = "",
    val dateError: String = "",
    val codeError: String = ""
)

class RestaurantCouponsViewModel @Inject constructor(
    private val couponRepo: CouponRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CouponsUiState())
    val state: StateFlow<CouponsUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(CouponFormState())
    val formState: StateFlow<CouponFormState> = _formState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init { loadRestaurantAndCoupons() }

    private fun loadRestaurantAndCoupons() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val query = firestore.collection("restaurants")
                    .whereEqualTo("ownerId", uid).limit(1).get().await()
                val restaurantId = query.documents.firstOrNull()?.id ?: return@launch
                _state.value = _state.value.copy(restaurantId = restaurantId)
                loadCoupons(restaurantId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar")
            }
        }
    }

    private suspend fun loadCoupons(restaurantId: String) {
        val result = couponRepo.getCouponsByRestaurant(restaurantId)
        result.fold(
            onSuccess = { list ->
                _state.value = _state.value.copy(coupons = list, isLoading = false)
            },
            onFailure = { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar cupons")
            }
        )
    }

    fun reload() { loadRestaurantAndCoupons() }

    fun deleteCoupon(couponId: String) {
        viewModelScope.launch {
            couponRepo.deleteCoupon(couponId)
            loadRestaurantAndCoupons()
        }
    }

    fun toggleCouponActive(coupon: Coupon) {
        viewModelScope.launch {
            couponRepo.updateCoupon(coupon.copy(active = !coupon.active))
            loadRestaurantAndCoupons()
        }
    }

    // Form
    fun onTitleChange(v: String) = updateForm { copy(title = v, titleError = "") }
    fun onDescriptionChange(v: String) = updateForm { copy(description = v) }
    fun onBenefitTypeChange(v: String) = updateForm { copy(benefitType = v, discountValue = "") }
    fun onDiscountValueChange(v: String) = updateForm { copy(discountValue = v, discountError = "") }
    fun onInternalCodeChange(v: String) = updateForm { copy(internalCode = v.uppercase(), codeError = "") }
    fun onRulesChange(v: String) = updateForm { copy(rules = v) }
    fun onValidUntilChange(v: String) = updateForm { copy(validUntilStr = v, dateError = "") }
    fun onMaxUsesChange(v: String) = updateForm { copy(maxUses = v) }

    fun resetForm() { _formState.value = CouponFormState() }

    fun loadCouponForEdit(coupon: Coupon) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        _formState.value = CouponFormState(
            title = coupon.title,
            description = coupon.description,
            benefitType = coupon.benefitType,
            discountValue = coupon.discountValue,
            internalCode = coupon.internalCode,
            rules = coupon.rules,
            validUntilStr = sdf.format(coupon.validUntil),
            maxUses = coupon.maxUses.toString()
        )
    }

    fun saveCoupon(editingCoupon: Coupon? = null) {
        val f = _formState.value
        val s = _state.value
        var hasError = false

        if (f.title.trim().length < 3) {
            updateForm { copy(titleError = "Título muito curto") }
            hasError = true
        }
        if (f.discountValue.isBlank()) {
            updateForm { copy(discountError = "Informe o desconto") }
            hasError = true
        }
        if (f.internalCode.isBlank()) {
            updateForm { copy(codeError = "Informe o código interno") }
            hasError = true
        }

        // Valida e converte data
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        sdf.isLenient = false
        val validUntil: Date = try {
            sdf.parse(f.validUntilStr) ?: throw Exception()
        } catch (e: Exception) {
            updateForm { copy(dateError = "Data inválida. Use dd/MM/aaaa") }
            hasError = true
            Date()
        }
        if (hasError) return

        viewModelScope.launch {
            updateForm { copy(isLoading = true, error = "") }
            try {
                val coupon = Coupon(
                    id = editingCoupon?.id ?: "",
                    restaurantId = s.restaurantId,
                    title = f.title.trim(),
                    description = f.description.trim(),
                    benefitType = f.benefitType,
                    discountValue = f.discountValue.trim(),
                    internalCode = f.internalCode.trim().uppercase(),
                    rules = f.rules.trim(),
                    validFrom = Date(),
                    validUntil = validUntil,
                    maxUses = f.maxUses.toIntOrNull() ?: 100,
                    active = true
                )

                val result = if (editingCoupon != null) {
                    couponRepo.updateCoupon(coupon)
                    Result.success("updated")
                } else {
                    couponRepo.createCoupon(coupon).map { it }
                }

                result.fold(
                    onSuccess = {
                        updateForm { copy(isLoading = false) }
                        resetForm()
                        loadRestaurantAndCoupons()
                    },
                    onFailure = { e ->
                        updateForm { copy(isLoading = false, error = e.message ?: "Erro ao salvar") }
                    }
                )
            } catch (e: Exception) {
                updateForm { copy(isLoading = false, error = e.message ?: "Erro ao salvar") }
            }
        }
    }

    private fun updateForm(block: CouponFormState.() -> CouponFormState) {
        _formState.value = _formState.value.block()
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun RestaurantCouponsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: RestaurantCouponsViewModel = appViewModel { RestaurantCouponsViewModel(it.couponRepository()) }
) {
    val state by viewModel.state.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editingCoupon by remember { mutableStateOf<Coupon?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Coupon?>(null) }

    // Dialog de confirmação de exclusão
    showDeleteDialog?.let { coupon ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir cupom?", fontWeight = FontWeight.Bold) },
            text = { Text("O cupom \"${coupon.title}\" será excluído permanentemente.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCoupon(coupon.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DiscountRed),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Excluir", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }, shape = RoundedCornerShape(10.dp)) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Formulário de criação/edição
    if (showForm) {
        CouponFormSheet(
            formState = formState,
            isEditing = editingCoupon != null,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onBenefitTypeChange = viewModel::onBenefitTypeChange,
            onDiscountValueChange = viewModel::onDiscountValueChange,
            onInternalCodeChange = viewModel::onInternalCodeChange,
            onRulesChange = viewModel::onRulesChange,
            onValidUntilChange = viewModel::onValidUntilChange,
            onMaxUsesChange = viewModel::onMaxUsesChange,
            onSave = {
                viewModel.saveCoupon(editingCoupon)
                if (formState.error.isEmpty() && !formState.isLoading) {
                    showForm = false
                    editingCoupon = null
                }
            },
            onDismiss = {
                showForm = false
                editingCoupon = null
                viewModel.resetForm()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Gerenciar Cupons", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (state.coupons.isNotEmpty()) {
                            Text("${state.coupons.size} cupons cadastrados", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                    FloatingActionButton(
                        onClick = {
                            viewModel.resetForm()
                            editingCoupon = null
                            showForm = true
                        },
                        containerColor = PrimaryGreen,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        bottomBar = {
            RestaurantBottomBar(currentRoute = "restaurant_coupons") { route ->
                when (route) {
                    "restaurant_home" -> onNavigateToHome()
                    "restaurant_dashboard" -> onNavigateToDashboard()
                    "restaurant_profile" -> onNavigateToProfile()
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

        if (state.error.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error, color = DiscountRed, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton("Tentar novamente", viewModel::reload)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Resumo
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryChip("Total", "${state.coupons.size}", Modifier.weight(1f))
                    SummaryChip("Ativos", "${state.coupons.count { it.active }}", Modifier.weight(1f), PrimaryGreen)
                    SummaryChip("Resgates", "${state.coupons.sumOf { it.redeemCount }}", Modifier.weight(1f), DiscountRed)
                }
            }

            if (state.coupons.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalOffer, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Nenhum cupom cadastrado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Crie seu primeiro cupom para atrair clientes", color = TextMuted, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton("+ Criar Cupom", {
                                viewModel.resetForm()
                                showForm = true
                            }, Modifier.fillMaxWidth())
                        }
                    }
                }
            } else {
                item { Text("Seus Cupons", fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                items(state.coupons, key = { it.id }) { coupon ->
                    CouponCard(
                        coupon = coupon,
                        onEdit = {
                            viewModel.loadCouponForEdit(coupon)
                            editingCoupon = coupon
                            showForm = true
                        },
                        onDelete = { showDeleteDialog = coupon },
                        onToggleActive = { viewModel.toggleCouponActive(coupon) }
                    )
                }

                item {
                    SecondaryButton("+ Criar Novo Cupom", {
                        viewModel.resetForm()
                        editingCoupon = null
                        showForm = true
                    }, Modifier.fillMaxWidth())
                }
            }

            // Dicas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Dicas para criar cupons eficazes", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "Use descontos atraentes (15% ou mais) para aumentar conversões",
                            "Crie cupons específicos para dias de menor movimento",
                            "Defina validades curtas para gerar senso de urgência",
                            "Os clientes escaneiam o QR Code no restaurante para resgatar"
                        ).forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                Text(tip, fontSize = 13.sp, color = TextDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Coupon Card ──────────────────────────────────────────────────────────────

@Composable
private fun CouponCard(
    coupon: Coupon,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, if (coupon.active) PrimaryGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Código e status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(TextDark, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(coupon.internalCode, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (coupon.active) StatusOpenBg else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .clickable { onToggleActive() }
                    ) {
                        Text(
                            if (coupon.active) "Ativo" else "Inativo",
                            color = if (coupon.active) StatusOpen else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(DiscountRed, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(coupon.discountValue, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(coupon.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (coupon.description.isNotEmpty()) {
                Text(coupon.description, fontSize = 13.sp, color = TextMuted)
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Válido até ${sdf.format(coupon.validUntil)}", fontSize = 12.sp, color = TextMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${coupon.redeemCount} resgates", fontSize = 12.sp, color = PrimaryGreen)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) {
                    Icon(Icons.Default.Edit, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar", color = PrimaryGreen)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DiscountRed)
                ) {
                    Icon(Icons.Default.Delete, null, tint = DiscountRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Excluir", color = DiscountRed)
                }
            }
        }
    }
}

// ─── Coupon Form ──────────────────────────────────────────────────────────────

@Composable
private fun CouponFormSheet(
    formState: CouponFormState,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBenefitTypeChange: (String) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onInternalCodeChange: (String) -> Unit,
    onRulesChange: (String) -> Unit,
    onValidUntilChange: (String) -> Unit,
    onMaxUsesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val discountPlaceholder = when (formState.benefitType) {
        "PERCENTAGE" -> "Ex: 20%"
        "FIXED" -> "Ex: R$10"
        else -> "Ex: Sobremesa grátis"
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (isEditing) "Editar Cupom" else "Novo Cupom",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fechar")
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (formState.error.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                formState.error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    PrimaryButton(
                        text = if (isEditing) "Salvar alterações" else "Criar Cupom",
                        onClick = onSave,
                        isLoading = formState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Informações básicas
            FormCard(title = "Informações do Cupom") {
                RoleMatchTextField(
                    value = formState.title,
                    onValueChange = onTitleChange,
                    label = "Título",
                    isError = formState.titleError.isNotEmpty(),
                    errorMessage = formState.titleError,
                    placeholder = "Ex: 20% OFF no jantar"
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descrição") },
                    placeholder = { Text("Válido de segunda a quinta-feira", color = TextLight) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tipo de desconto
            FormCard(title = "Tipo de Desconto") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PERCENTAGE" to "Percentual", "FIXED" to "Fixo", "FREE_ITEM" to "Item Grátis")
                        .forEach { (type, label) ->
                            val selected = formState.benefitType == type
                            Button(
                                onClick = { onBenefitTypeChange(type) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) Color.White else TextDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text(label, fontSize = 12.sp) }
                        }
                }
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(
                    value = formState.discountValue,
                    onValueChange = onDiscountValueChange,
                    label = "Valor do desconto",
                    isError = formState.discountError.isNotEmpty(),
                    errorMessage = formState.discountError,
                    placeholder = discountPlaceholder
                )
            }

            // Código e validade
            FormCard(title = "Código e Validade") {
                RoleMatchTextField(
                    value = formState.internalCode,
                    onValueChange = onInternalCodeChange,
                    label = "Código interno",
                    isError = formState.codeError.isNotEmpty(),
                    errorMessage = formState.codeError,
                    placeholder = "Ex: JANTAR20"
                )
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(
                    value = formState.validUntilStr,
                    onValueChange = onValidUntilChange,
                    label = "Válido até",
                    isError = formState.dateError.isNotEmpty(),
                    errorMessage = formState.dateError,
                    placeholder = "dd/MM/aaaa",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(
                    value = formState.maxUses,
                    onValueChange = onMaxUsesChange,
                    label = "Máximo de usos",
                    placeholder = "100",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Regras (opcional)
            FormCard(title = "Regras (opcional)") {
                OutlinedTextField(
                    value = formState.rules,
                    onValueChange = onRulesChange,
                    label = { Text("Regras e restrições") },
                    placeholder = { Text("Válido apenas para consumo no local...", color = TextLight) },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SummaryChip(label: String, value: String, modifier: Modifier, valueColor: Color = TextDark) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = valueColor)
            Text(label, fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
private fun FormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}