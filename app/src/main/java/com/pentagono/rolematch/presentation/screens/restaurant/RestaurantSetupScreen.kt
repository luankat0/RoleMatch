package com.pentagono.rolematch.presentation.screens.restaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.core.theme.BorderLight
import com.pentagono.rolematch.core.theme.DarkGreen
import com.pentagono.rolematch.core.theme.DividerLight
import com.pentagono.rolematch.core.theme.LightGreen
import com.pentagono.rolematch.core.theme.PrimaryGreen
import com.pentagono.rolematch.core.theme.StatusPending
import com.pentagono.rolematch.core.theme.TextDark
import com.pentagono.rolematch.core.theme.TextLight
import com.pentagono.rolematch.core.theme.TextMuted
import com.pentagono.rolematch.core.theme.WarningYellow
import com.pentagono.rolematch.core.validators.CnpjValidator
import com.pentagono.rolematch.di.appViewModel
import com.pentagono.rolematch.domain.model.Address
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.model.RestaurantFeatures
import com.pentagono.rolematch.domain.model.RestaurantStatus
import com.pentagono.rolematch.domain.model.ServiceMode
import com.pentagono.rolematch.domain.repository.RestaurantRepository
import com.pentagono.rolematch.presentation.components.ConfirmCancelDialog
import com.pentagono.rolematch.presentation.components.PrimaryButton
import com.pentagono.rolematch.presentation.components.RoleMatchLogo
import com.pentagono.rolematch.presentation.components.RoleMatchTextField
import com.pentagono.rolematch.presentation.components.SelectableChip
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestaurantSetupState(
    val step: Int = 0,
    val restaurantName: String = "",
    val cnpj: String = "",
    val establishmentType: String = "",
    val description: String = "",
    val serviceMode: String = "Presencial",
    val openingHours: Map<String, Pair<String, String>> = defaultHours(),
    val phone: String = "",
    val whatsapp: String = "",
    val instagram: String = "",
    val acceptsVr: Boolean = false,
    val petFriendly: Boolean = false,
    val kidsArea: Boolean = false,
    val acceptsReservations: Boolean = true,
    val cuisineTypes: Set<String> = emptySet(),
    val menuLink: String = "",
    val cep: String = "",
    val street: String = "",
    val number: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val photoCount: Int = 0,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val showCancelDialog: Boolean = false,
    val nameError: String = "",
    val cnpjError: String = "",
    val generalError: String = ""
)

private fun defaultHours() = mapOf(
    "Segunda" to ("Fechado" to ""),
    "Terça" to ("18:00" to "23:00"),
    "Quarta" to ("18:00" to "23:00"),
    "Quinta" to ("18:00" to "00:00"),
    "Sexta" to ("12:00" to "01:00"),
    "Sábado" to ("12:00" to "01:00"),
    "Domingo" to ("12:00" to "22:00")
)

class RestaurantSetupViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RestaurantSetupState())
    val state: StateFlow<RestaurantSetupState> = _state.asStateFlow()

    // Canal de evento único para navegação — não duplica em recomposição
    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onNameChange(value: String) = update { copy(restaurantName = value, nameError = "") }
    fun onCnpjChange(value: String) = update { copy(cnpj = CnpjValidator.format(value), cnpjError = "") }
    fun onDescriptionChange(value: String) = update { copy(description = value) }
    fun onEstablishmentTypeChange(value: String) = update { copy(establishmentType = value) }
    fun onServiceModeChange(value: String) = update { copy(serviceMode = value) }
    fun onPhoneChange(value: String) = update { copy(phone = value) }
    fun onWhatsappChange(value: String) = update { copy(whatsapp = value) }
    fun onInstagramChange(value: String) = update { copy(instagram = value) }
    fun onAcceptsVrToggle() = update { copy(acceptsVr = !acceptsVr) }
    fun onPetFriendlyToggle() = update { copy(petFriendly = !petFriendly) }
    fun onKidsAreaToggle() = update { copy(kidsArea = !kidsArea) }
    fun onReservationsToggle(value: Boolean) = update { copy(acceptsReservations = value) }
    fun onCuisineToggle(type: String) = update {
        copy(cuisineTypes = if (type in cuisineTypes) cuisineTypes - type else cuisineTypes + type)
    }
    fun onOpeningHourChange(day: String, open: String, close: String) = update {
        copy(openingHours = openingHours + (day to (open to close)))
    }
    fun onCepChange(value: String) = update { copy(cep = value) }
    fun onStreetChange(value: String) = update { copy(street = value) }
    fun onNumberChange(value: String) = update { copy(number = value) }
    fun onNeighborhoodChange(value: String) = update { copy(neighborhood = value) }
    fun onCityChange(value: String) = update { copy(city = value) }
    fun onMenuLinkChange(value: String) = update { copy(menuLink = value) }
    fun onAddPhoto() = update { if (photoCount < 9) copy(photoCount = photoCount + 1) else this }
    fun onShowCancelDialog(show: Boolean) = update { copy(showCancelDialog = show) }
    fun onNavigationHandled() = update { copy(isCompleted = false) } // mantido para compatibilidade

    fun nextStep() {
        val s = _state.value
        when (s.step) {
            0 -> {
                var hasError = false
                if (s.restaurantName.trim().length < 2) {
                    update { copy(nameError = "Nome obrigatório") }
                    hasError = true
                }
                if (!CnpjValidator.isValid(s.cnpj)) {
                    update { copy(cnpjError = "CNPJ inválido") }
                    hasError = true
                }
                if (!hasError) update { copy(step = 1) }
            }
            1, 2, 3 -> update { copy(step = step + 1) }
            4 -> submit()
        }
    }

    fun prevStep() = update { if (step > 0) copy(step = step - 1) else this }

    private fun submit() {
        viewModelScope.launch {
            update { copy(isSubmitting = true, generalError = "") }
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("Sessão expirada. Faça login novamente.")
                val s = _state.value
                val serviceMode = when (s.serviceMode) {
                    "Delivery" -> ServiceMode.DELIVERY
                    "Ambos" -> ServiceMode.BOTH
                    else -> ServiceMode.PRESENTIAL
                }
                val restaurant = Restaurant(
                    ownerId = uid,
                    name = s.restaurantName.trim(),
                    cnpj = CnpjValidator.strip(s.cnpj),
                    description = s.description.trim(),
                    establishmentTypes = if (s.establishmentType.isNotEmpty()) listOf(s.establishmentType) else emptyList(),
                    cuisineTypes = s.cuisineTypes.toList(),
                    address = Address(
                        cep = s.cep.trim(),
                        street = s.street.trim(),
                        number = s.number.trim(),
                        neighborhood = s.neighborhood.trim(),
                        city = s.city.trim()
                    ),
                    phone = s.phone.trim(),
                    whatsapp = s.whatsapp.trim(),
                    instagram = s.instagram.trim(),
                    menuLink = s.menuLink.trim(),
                    features = RestaurantFeatures(
                        petFriendly = s.petFriendly,
                        kidsArea = s.kidsArea,
                        acceptsVr = s.acceptsVr
                    ),
                    acceptsReservations = s.acceptsReservations,
                    serviceMode = serviceMode,
                    status = RestaurantStatus.PENDING_REVIEW
                )
                val result = restaurantRepository.createRestaurant(restaurant)
                result.fold(
                    onSuccess = {
                        update { copy(isSubmitting = false) }
                        _navigationEvent.send(Unit)
                    },
                    onFailure = { error ->
                        update { copy(isSubmitting = false, generalError = error.message ?: "Erro ao salvar restaurante") }
                    }
                )
            } catch (e: Exception) {
                update { copy(isSubmitting = false, generalError = e.message ?: "Erro ao salvar restaurante") }
            }
        }
    }

    private fun update(block: RestaurantSetupState.() -> RestaurantSetupState) {
        _state.value = _state.value.block()
    }
}

@Composable
fun RestaurantSetupScreen(
    onCancel: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: RestaurantSetupViewModel = appViewModel { RestaurantSetupViewModel(it.restaurantRepository()) }
) {
    val state by viewModel.state.collectAsState()

    // Evento único de navegação — não duplica em recomposição
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            onCompleted()
        }
    }

    if (state.showCancelDialog) {
        ConfirmCancelDialog(
            title = "Cancelar Cadastro?",
            message = "Tem certeza que deseja cancelar o cadastro do seu restaurante? Todas as informações preenchidas serão perdidas.",
            confirmLabel = "Sim, cancelar",
            dismissLabel = "Continuar cadastro",
            onConfirm = { viewModel.onShowCancelDialog(false); onCancel() },
            onDismiss = { viewModel.onShowCancelDialog(false) }
        )
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoleMatchLogo()
                        IconButton(onClick = { viewModel.onShowCancelDialog(true) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Etapa ${state.step + 1} de 5", fontSize = 12.sp, color = TextMuted)
                    Text("Configuração do Restaurante", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (state.step + 1) / 5f },
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryGreen,
                        trackColor = BorderLight
                    )
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (state.generalError.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                state.generalError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    PrimaryButton(
                        text = if (state.step == 4) "Finalizar Cadastro" else "Continuar",
                        onClick = viewModel::nextStep,
                        isLoading = state.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.step > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::prevStep, modifier = Modifier.fillMaxWidth()) {
                            Text("‹ Voltar", color = TextMuted)
                        }
                    }
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
            when (state.step) {
                0 -> Step1BasicInfo(state = state, vm = viewModel)
                1 -> Step2Characteristics(state = state, vm = viewModel)
                2 -> Step3Location(state = state, vm = viewModel)
                3 -> Step4Gallery(state = state, vm = viewModel)
                4 -> Step5Review(state = state)
            }
        }
    }
}

// ─── Step 1: Informações Básicas ──────────────────────────────────────────────

@Composable
private fun Step1BasicInfo(state: RestaurantSetupState, vm: RestaurantSetupViewModel) {
    SetupCard(title = "Informações Básicas", subtitle = "Dados essenciais do seu estabelecimento") {
        // Logo placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clickable { /* TODO: image picker */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Upload, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                Text("Clique para fazer upload", fontSize = 13.sp, color = TextMuted)
                Text("PNG, JPG até 5MB", fontSize = 11.sp, color = TextLight)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        RoleMatchTextField(
            value = state.establishmentType,
            onValueChange = vm::onEstablishmentTypeChange,
            label = "Tipo de estabelecimento",
            placeholder = "Ex: Restaurante"
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleMatchTextField(
            value = state.restaurantName,
            onValueChange = vm::onNameChange,
            label = "Nome do Restaurante",
            isError = state.nameError.isNotEmpty(),
            errorMessage = state.nameError,
            placeholder = "Ex: Rolê Match Bistrô"
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleMatchTextField(
            value = state.cnpj,
            onValueChange = vm::onCnpjChange,
            label = "CNPJ",
            leadingIcon = Icons.Default.Business,
            isError = state.cnpjError.isNotEmpty(),
            errorMessage = state.cnpjError,
            placeholder = "00.000.000/0000-00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = vm::onDescriptionChange,
            label = { Text("Resumo do seu estabelecimento") },
            placeholder = { Text("Conte um pouco sobre seu restaurante...", color = TextLight) },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderLight
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }

    SetupCard(title = "Modalidade de Atendimento", subtitle = "Como seu restaurante atende os clientes?") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Presencial", "Delivery", "Ambos").forEach { mode ->
                val selected = state.serviceMode == mode
                Button(
                    onClick = { vm.onServiceModeChange(mode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) Color.White else TextDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text(mode, fontSize = 13.sp) }
            }
        }
    }

    // ─── Horários editáveis ───────────────────────────────────────────────────
    SetupCard(title = "Horário de Funcionamento", subtitle = "Toque em um dia para editar") {
        val days = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
        var editingDay by remember { mutableStateOf<String?>(null) }

        days.forEach { day ->
            val times = state.openingHours[day] ?: ("Fechado" to "")
            val isClosed = times.first == "Fechado"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingDay = day }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(day, fontSize = 14.sp, modifier = Modifier.width(80.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isClosed) "Fechado" else "${times.first} — ${times.second}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isClosed) TextMuted else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Edit, null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                }
            }
            HorizontalDivider(color = DividerLight)
        }

        // Dialog de edição de horário
        editingDay?.let { day ->
            val currentTimes = state.openingHours[day] ?: ("Fechado" to "")
            val isClosed = currentTimes.first == "Fechado"
            var openTime by remember(day) { mutableStateOf(if (isClosed) "18:00" else currentTimes.first) }
            var closeTime by remember(day) { mutableStateOf(if (isClosed) "23:00" else currentTimes.second) }
            var closed by remember(day) { mutableStateOf(isClosed) }

            AlertDialog(
                onDismissRequest = { editingDay = null },
                title = { Text("Horário — $day", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fechado neste dia", modifier = Modifier.weight(1f))
                            androidx.compose.material3.Switch(
                                checked = closed,
                                onCheckedChange = { closed = it },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryGreen
                                )
                            )
                        }
                        if (!closed) {
                            OutlinedTextField(
                                value = openTime,
                                onValueChange = { openTime = it },
                                label = { Text("Abre") },
                                placeholder = { Text("18:00") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = BorderLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = closeTime,
                                onValueChange = { closeTime = it },
                                label = { Text("Fecha") },
                                placeholder = { Text("23:00") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = BorderLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (closed) {
                                vm.onOpeningHourChange(day, "Fechado", "")
                            } else {
                                vm.onOpeningHourChange(day, openTime, closeTime)
                            }
                            editingDay = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Salvar") }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { editingDay = null },
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Cancelar") }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    SetupCard(title = "Contato e Redes Sociais", subtitle = "Formas de contato com seu restaurante") {
        RoleMatchTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            label = "Telefone",
            placeholder = "(11) 98765-4321"
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleMatchTextField(
            value = state.whatsapp,
            onValueChange = vm::onWhatsappChange,
            label = "WhatsApp (opcional)",
            placeholder = "(11) 98765-4321"
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleMatchTextField(
            value = state.instagram,
            onValueChange = vm::onInstagramChange,
            label = "Instagram (opcional)",
            placeholder = "@seurestaurante"
        )
    }
}

// ─── Step 2: Características ──────────────────────────────────────────────────
// Removido: Nome e Telefone (já coletados na etapa 1)
// Corrigido: FlowRow substituído por Column+Row para compatibilidade

@Composable
private fun Step2Characteristics(state: RestaurantSetupState, vm: RestaurantSetupViewModel) {
    SetupCard(title = "Características do Estabelecimento", subtitle = "Ajude os clientes a encontrarem seu restaurante") {
        Text("Serviços e facilidades", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableChip("Aceita VR", state.acceptsVr, vm::onAcceptsVrToggle)
            SelectableChip("🐾 Pet Friendly", state.petFriendly, vm::onPetFriendlyToggle)
            SelectableChip("👶 Área Kids", state.kidsArea, vm::onKidsAreaToggle)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("Reservas", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { vm.onReservationsToggle(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.acceptsReservations) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.acceptsReservations) Color.White else TextDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) { Text("Aceita Reservas") }
            Button(
                onClick = { vm.onReservationsToggle(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!state.acceptsReservations) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!state.acceptsReservations) Color.White else TextDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) { Text("Não Aceita") }
        }
    }

    // Tipo de Cozinha — FlowRow substituído por Column+Row manual
    SetupCard(title = "Tipo de Cozinha", subtitle = "Selecione todos que se aplicam") {
        val categories = MockData.cuisineCategories
        val chunked = categories.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chunked.forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { type ->
                        SelectableChip(
                            label = type,
                            selected = type in state.cuisineTypes,
                            onClick = { vm.onCuisineToggle(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Preenche espaço se linha incompleta
                    repeat(3 - rowItems.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }

    SetupCard(title = "Cardápio Digital", subtitle = "Compartilhe o cardápio do seu restaurante") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Icon(Icons.Default.Link, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Link do Cardápio", color = TextDark)
            }
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Icon(Icons.Default.PictureAsPdf, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload PDF", color = TextDark)
            }
        }
    }
}

// ─── Step 3: Localização ──────────────────────────────────────────────────────

@Composable
private fun Step3Location(state: RestaurantSetupState, vm: RestaurantSetupViewModel) {
    SetupCard(title = "Localização", subtitle = "Insira o endereço para que os clientes encontrem seu restaurante facilmente") {
        RoleMatchTextField(
            value = state.cep,
            onValueChange = vm::onCepChange,
            label = "CEP",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = "00000-000"
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoleMatchTextField(
                value = state.street,
                onValueChange = vm::onStreetChange,
                label = "Logradouro",
                placeholder = "Av. Principal",
                modifier = Modifier.weight(2f)
            )
            RoleMatchTextField(
                value = state.number,
                onValueChange = vm::onNumberChange,
                label = "Número",
                placeholder = "123",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        RoleMatchTextField(
            value = state.neighborhood,
            onValueChange = vm::onNeighborhoodChange,
            label = "Bairro",
            placeholder = "Centro"
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleMatchTextField(
            value = state.city,
            onValueChange = vm::onCityChange,
            label = "Cidade",
            placeholder = "São Paulo"
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Map, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                Text("Pré-visualização do mapa", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

// ─── Step 4: Galeria ──────────────────────────────────────────────────────────

@Composable
private fun Step4Gallery(state: RestaurantSetupState, vm: RestaurantSetupViewModel) {
    SetupCard(title = "Galeria de Fotos", subtitle = "Adicione fotos do ambiente e dos pratos para atrair mais clientes") {
        val rows = (0 until 9).chunked(3)
        rows.forEach { columns ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                columns.forEach { index ->
                    val hasPhoto = index < state.photoCount
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .background(
                                if (hasPhoto) PrimaryGreen.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                enabled = !hasPhoto,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { vm.onAddPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasPhoto) {
                            Icon(Icons.Default.Check, null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Upload, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                                Text("Adicionar", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                "Dica: Adicione pelo menos 3 fotos do ambiente e 3 dos pratos principais.\nFormatos aceitos: JPG, PNG • Tamanho máximo: 5MB por foto",
                fontSize = 12.sp,
                color = DarkGreen,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// ─── Step 5: Revisão ──────────────────────────────────────────────────────────

@Composable
private fun Step5Review(state: RestaurantSetupState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Quase lá! 🎉", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
            Text(
                "Revise as informações do seu restaurante antes de finalizar",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }
    }

    SetupCard(title = "Informações Básicas") {
        ReviewRow("Nome", state.restaurantName.ifEmpty { "—" })
        ReviewRow("Tipo", state.establishmentType.ifEmpty { "—" })
        ReviewRow("CNPJ", state.cnpj.ifEmpty { "—" })
        ReviewRow("Telefone", state.phone.ifEmpty { "—" })
    }

    SetupCard(title = "Localização") {
        val address = buildString {
            if (state.street.isNotEmpty()) append("${state.street}, ${state.number}")
            if (state.neighborhood.isNotEmpty()) append(" – ${state.neighborhood}")
            if (state.city.isNotEmpty()) append(", ${state.city}")
        }
        ReviewRow("Endereço", address.ifEmpty { "—" })
        ReviewRow("CEP", state.cep.ifEmpty { "—" })
    }

    SetupCard(title = "Características") {
        val features = buildList {
            if (state.acceptsVr) add("Aceita VR")
            if (state.petFriendly) add("Pet Friendly")
            if (state.kidsArea) add("Área Kids")
            if (state.acceptsReservations) add("Aceita Reservas")
        }
        if (features.isEmpty()) {
            Text("—", color = TextMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                features.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { feature ->
                            Box(
                                modifier = Modifier
                                    .background(LightGreen, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(feature, fontSize = 12.sp, color = DarkGreen)
                            }
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Info, null, tint = StatusPending, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Após finalizar, seu restaurante ficará com status Pendente de Aprovação. Um administrador irá revisar e ativar seu perfil em até 48 horas.",
                fontSize = 13.sp,
                color = TextDark
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
    HorizontalDivider(color = DividerLight, modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
private fun SetupCard(
    title: String,
    subtitle: String = "",
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 13.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            content()
        }
    }
}