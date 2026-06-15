package com.pentagono.rolematch.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.core.validators.EmailValidator
import com.pentagono.rolematch.di.appViewModel
import com.pentagono.rolematch.domain.repository.AuthRepository
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String = "",
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    val error: String = ""
)

class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, emailError = "", error = "")
    }

    fun onSend() {
        val email = _state.value.email.trim()

        if (!EmailValidator.isValid(email)) {
            _state.value = _state.value.copy(emailError = "E-mail inválido")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")

            val result = authRepository.sendPasswordResetEmail(email)

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false, isSent = true)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao enviar e-mail"
                    )
                }
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onEmailSent: () -> Unit,
    viewModel: ForgotPasswordViewModel = appViewModel { ForgotPasswordViewModel(it.authRepository()) }
) {
    val state by viewModel.state.collectAsState()

    if (state.isSent) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = StatusOpen,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "E-mail enviado!",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Enviamos um link para:\n${state.email}\n\nVerifique sua caixa de entrada e siga as instruções para redefinir sua senha.\n\nNão esqueça de checar a pasta de spam.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(36.dp))
            PrimaryButton(
                text = "Voltar ao login",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
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
                Box(
                    modifier = Modifier.size(72.dp).background(LightGreen, RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = PrimaryGreen, modifier = Modifier.size(36.dp))
                }

                Spacer(Modifier.height(20.dp))
                Text("Esqueceu a senha?", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Digite seu e-mail e enviaremos um link para redefinir sua senha.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                RoleMatchTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "E-mail",
                    leadingIcon = Icons.Default.MailOutline,
                    isError = state.emailError.isNotEmpty(),
                    errorMessage = state.emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    placeholder = "seu@email.com"
                )

                if (state.error.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            state.error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                PrimaryButton(
                    text = "Enviar link de redefinição",
                    onClick = viewModel::onSend,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onBack) {
                    Text("Voltar ao login", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Dica", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Verifique também a pasta de spam ou lixo eletrônico caso não encontre o e-mail na caixa de entrada.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (state.isLoading) {
        LoadingOverlay("Enviando e-mail...")
    }
}