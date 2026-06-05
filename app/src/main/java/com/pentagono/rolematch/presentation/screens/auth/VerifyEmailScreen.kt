package com.pentagono.rolematch.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pentagono.rolematch.core.CredentialHolder
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.data.repository.RepositoryProvider
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── ViewModel ────────────────────────────────────────────────────────────────

data class VerifyEmailState(
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val isVerified: Boolean = false,
    val error: String = "",
    val successMessage: String = "",
    val canResend: Boolean = true,
    val resendCountdown: Int = 0
)

class VerifyEmailViewModel : ViewModel() {

    private val _state = MutableStateFlow(VerifyEmailState())
    val state: StateFlow<VerifyEmailState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val authRepository = RepositoryProvider.auth

    fun checkVerification() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            try {
                // Se currentUser for null, reautentica com as credenciais salvas no cadastro
                if (auth.currentUser == null) {
                    val email = CredentialHolder.email
                    val password = CredentialHolder.password
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        android.util.Log.d("VerifyEmail", "currentUser null — reautenticando: $email")
                        auth.signInWithEmailAndPassword(email, password).await()
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Sessão expirada. Cancele e faça login novamente."
                        )
                        return@launch
                    }
                }

                delay(1000)
                // Força refresh do token para limpar cache
                auth.currentUser?.getIdToken(true)?.await()
                auth.currentUser?.reload()?.await()

                val user = auth.currentUser
                android.util.Log.d("VerifyEmail", "user: ${user?.email}")
                android.util.Log.d("VerifyEmail", "isEmailVerified: ${user?.isEmailVerified}")

                val verified = user?.isEmailVerified == true
                if (verified) {
                    CredentialHolder.clear()
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    isVerified = verified,
                    error = if (!verified) "E-mail ainda não verificado. Verifique sua caixa de entrada." else ""
                )
            } catch (e: Exception) {
                android.util.Log.e("VerifyEmail", "Erro: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao verificar"
                )
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isResending = true, error = "", successMessage = "")
            val result = authRepository.sendVerificationCode("")
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isResending = false,
                        successMessage = "E-mail reenviado com sucesso!",
                        canResend = false,
                        resendCountdown = 60
                    )
                    startCountdown()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isResending = false,
                        error = e.message ?: "Erro ao reenviar e-mail"
                    )
                }
            )
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            for (i in 60 downTo 1) {
                _state.value = _state.value.copy(resendCountdown = i)
                delay(1000)
            }
            _state.value = _state.value.copy(canResend = true, resendCountdown = 0)
        }
    }

    fun onNavigationHandled() {
        _state.value = _state.value.copy(isVerified = false)
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun VerifyEmailScreen(
    accountType: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: VerifyEmailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
        ?: CredentialHolder.email.ifEmpty { "" }

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            viewModel.onNavigationHandled()
            onVerified()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        RoleMatchLogo()
        Spacer(Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(LightGreen, RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MailOutline,
                        null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    "Verifique seu e-mail",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enviamos um link de verificação para:",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    userEmail,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Info box
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = DarkGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Como verificar:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkGreen)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("1. Abra seu e-mail", fontSize = 13.sp, color = DarkGreen)
                        Text("2. Procure o e-mail do Rolê Match", fontSize = 13.sp, color = DarkGreen)
                        Text("3. Clique no link de verificação", fontSize = 13.sp, color = DarkGreen)
                        Text("4. Volte aqui e toque em \"Já verifiquei\"", fontSize = 13.sp, color = DarkGreen)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Erros e mensagens
                if (state.error.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            state.error,
                            color = DiscountRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (state.successMessage.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusOpenBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = StatusOpen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(state.successMessage, color = StatusOpen, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Botão principal
                PrimaryButton(
                    text = "✓ Já verifiquei meu e-mail",
                    onClick = viewModel::checkVerification,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Reenviar
                if (state.canResend) {
                    SecondaryButton(
                        text = "Reenviar e-mail",
                        onClick = viewModel::resendVerificationEmail,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Reenviar em ${state.resendCountdown}s",
                            color = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    CredentialHolder.clear()
                    FirebaseAuth.getInstance().signOut()
                    onBack()
                }) {
                    Text("Cancelar e voltar", color = TextMuted)
                }
            }
        }
    }

    if (state.isResending) {
        LoadingOverlay("Reenviando e-mail...")
    }
}