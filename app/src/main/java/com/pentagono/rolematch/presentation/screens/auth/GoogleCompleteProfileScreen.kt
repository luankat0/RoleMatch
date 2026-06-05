package com.pentagono.rolematch.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.core.validators.CpfValidator
import com.pentagono.rolematch.core.validators.PhoneValidator
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── State & ViewModel ────────────────────────────────────────────────────────

data class GoogleProfileState(
    val phone: String = "",
    val cpf: String = "",
    val phoneError: String = "",
    val cpfError: String = "",
    val isLoading: Boolean = false,
    val generalError: String = "",
    val navigateToHome: Boolean = false
)

class GoogleCompleteProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(GoogleProfileState())
    val state: StateFlow<GoogleProfileState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun onPhoneChange(v: String) {
        val digits = v.replace(Regex("[^0-9]"), "")
        update { copy(phone = PhoneValidator.format(digits), phoneError = "") }
    }

    fun onCpfChange(v: String) {
        val digits = v.replace(Regex("[^0-9]"), "")
        update { copy(cpf = CpfValidator.format(digits), cpfError = "") }
    }

    fun onNavigationHandled() = update { copy(navigateToHome = false) }

    fun onSave() {
        val s = _state.value
        var hasError = false

        if (!PhoneValidator.isValid(s.phone)) {
            update { copy(phoneError = "Telefone inválido") }
            hasError = true
        }
        if (!CpfValidator.isValid(s.cpf)) {
            update { copy(cpfError = "CPF inválido") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            update { copy(isLoading = true, generalError = "") }
            try {
                val user = auth.currentUser
                    ?: throw Exception("Sessão expirada. Faça login novamente.")

                val userData = hashMapOf(
                    "name" to (user.displayName ?: ""),
                    "email" to (user.email ?: ""),
                    "phone" to s.phone,
                    "cpf" to CpfValidator.strip(s.cpf),
                    "role" to "USER",
                    "isVerified" to true,
                    "provider" to "google",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore
                    .collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()

                update { copy(isLoading = false, navigateToHome = true) }
            } catch (e: Exception) {
                update {
                    copy(
                        isLoading = false,
                        generalError = e.message ?: "Erro ao salvar dados"
                    )
                }
            }
        }
    }

    private fun update(block: GoogleProfileState.() -> GoogleProfileState) {
        _state.value = _state.value.block()
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun GoogleCompleteProfileScreen(
    onCompleted: () -> Unit,
    viewModel: GoogleCompleteProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val user = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(state.navigateToHome) {
        if (state.navigateToHome) {
            viewModel.onNavigationHandled()
            onCompleted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            RoleMatchLogo()
            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text(
                        "Quase lá!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Olá, ${user?.displayName?.split(" ")?.firstOrNull() ?: ""}! Precisamos de mais algumas informações.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    RoleMatchTextField(
                        value = state.phone,
                        onValueChange = viewModel::onPhoneChange,
                        label = "Telefone",
                        leadingIcon = Icons.Default.Call,
                        isError = state.phoneError.isNotEmpty(),
                        errorMessage = state.phoneError,
                        placeholder = "(11) 98765-4321",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    RoleMatchTextField(
                        value = state.cpf,
                        onValueChange = viewModel::onCpfChange,
                        label = "CPF",
                        leadingIcon = Icons.Default.CreditScore,
                        isError = state.cpfError.isNotEmpty(),
                        errorMessage = state.cpfError,
                        placeholder = "000.000.000-00",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    if (state.generalError.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
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
                    }

                    Spacer(Modifier.height(24.dp))
                    PrimaryButton(
                        text = "Concluir cadastro",
                        onClick = viewModel::onSave,
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (state.isLoading) LoadingOverlay("Salvando dados...")
    }
}