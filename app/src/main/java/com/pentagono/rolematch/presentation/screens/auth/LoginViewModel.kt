package com.pentagono.rolematch.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentagono.rolematch.core.security.SecurityManager
import com.pentagono.rolematch.core.validators.EmailValidator
import com.pentagono.rolematch.data.repository.FirebaseAuthRepository
import com.pentagono.rolematch.data.repository.RepositoryProvider
import com.pentagono.rolematch.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isRestaurant: Boolean = false,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val navigateToUserHome: Boolean = false,
    val navigateToRestaurantHome: Boolean = false,
    val navigateToAdminHome: Boolean = false,
    val navigateToCompleteProfile: Boolean = false  // Google: primeira vez
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val authRepository = RepositoryProvider.auth
    private val firebaseAuthRepository = authRepository as? FirebaseAuthRepository

    // Função para lançar o intent do Google (definida pela MainActivity)
    private var googleSignInLauncher: (() -> Unit)? = null

    fun setGoogleSignInLauncher(launcher: () -> Unit) {
        googleSignInLauncher = launcher
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = "", error = "")
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = "", error = "")
    }

    fun onAccountTypeChange(isRestaurant: Boolean) {
        _uiState.value = _uiState.value.copy(isRestaurant = isRestaurant, error = "")
    }

    fun onRememberMeChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = value)
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(
            navigateToUserHome = false,
            navigateToRestaurantHome = false,
            navigateToAdminHome = false,
            navigateToCompleteProfile = false
        )
    }

    fun onGoogleSignIn() {
        if (_uiState.value.isRestaurant) {
            _uiState.value = _uiState.value.copy(
                error = "Login com Google não está disponível para restaurantes."
            )
            return
        }
        googleSignInLauncher?.invoke()
            ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "Erro ao iniciar login com Google."
                )
            }
    }

    fun onGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val result = firebaseAuthRepository?.loginWithGoogle(idToken)
                ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro interno. Tente novamente."
                    )
                    return@launch
                }

            result.fold(
                onSuccess = { user ->
                    SecurityManager.startSession()
                    if (!user.isVerified) {
                        // Primeira vez com Google — precisa completar perfil
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            navigateToCompleteProfile = true
                        )
                    } else {
                        // Usuário já tem perfil completo
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            navigateToUserHome = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao fazer login com Google"
                    )
                }
            )
        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = message
        )
    }

    fun onLogin() {
        val state = _uiState.value

        if (!EmailValidator.isValid(state.email)) {
            _uiState.value = state.copy(emailError = "E-mail inválido")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(passwordError = "Senha muito curta")
            return
        }

        if (!SecurityManager.canAttemptLogin(state.email)) {
            val minutes = SecurityManager.getLockoutMinutes(state.email)
            _uiState.value = state.copy(
                error = "Muitas tentativas. Tente novamente em $minutes minuto(s)."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")

            val result = authRepository.loginWithEmail(state.email, state.password)

            result.fold(
                onSuccess = { user ->
                    SecurityManager.clearLoginAttempts(state.email)
                    SecurityManager.startSession()

                    val isRestaurantUser = user.role == UserRole.RESTAURANT
                    val isAdminUser = user.role == UserRole.ADMIN

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        navigateToUserHome = !isRestaurantUser && !isAdminUser,
                        navigateToRestaurantHome = isRestaurantUser,
                        navigateToAdminHome = isAdminUser
                    )
                },
                onFailure = { error ->
                    SecurityManager.recordFailedLogin(state.email)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao fazer login"
                    )
                }
            )
        }
    }
}