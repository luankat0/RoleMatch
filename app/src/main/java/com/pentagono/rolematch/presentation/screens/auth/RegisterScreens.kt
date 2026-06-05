package com.pentagono.rolematch.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pentagono.rolematch.core.CredentialHolder
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.core.validators.*
import com.pentagono.rolematch.data.repository.RepositoryProvider
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── Register User ────────────────────────────────────────────────────────────

data class RegisterUserState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val cpf: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String = "",
    val emailError: String = "",
    val phoneError: String = "",
    val cpfError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val termsError: String = "",
    val navigateToVerify: Boolean = false,
    val generalError: String = ""
)

class RegisterUserViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterUserState())
    val state: StateFlow<RegisterUserState> = _state.asStateFlow()

    private val authRepository = RepositoryProvider.auth

    fun onNameChange(v: String) = update { copy(name = v, nameError = "") }
    fun onEmailChange(v: String) = update { copy(email = v, emailError = "") }

    fun onPhoneChange(v: String) {
        val digits = v.replace(Regex("[^0-9]"), "")
        update { copy(phone = PhoneValidator.format(digits), phoneError = "") }
    }

    fun onCpfChange(v: String) {
        val digits = v.replace(Regex("[^0-9]"), "")
        update { copy(cpf = CpfValidator.format(digits), cpfError = "") }
    }

    fun onPasswordChange(v: String) = update { copy(password = v, passwordError = "") }
    fun onConfirmPasswordChange(v: String) =
        update { copy(confirmPassword = v, confirmPasswordError = "") }
    fun onTermsChange(v: Boolean) = update { copy(termsAccepted = v, termsError = "") }
    fun onNavigationHandled() = update { copy(navigateToVerify = false) }

    fun onRegister() {
        val s = _state.value
        var hasError = false

        if (s.name.trim().length < 3) {
            update { copy(nameError = "Nome muito curto") }
            hasError = true
        }
        if (!EmailValidator.isValid(s.email)) {
            update { copy(emailError = "E-mail inválido") }
            hasError = true
        }
        if (!PhoneValidator.isValid(s.phone)) {
            update { copy(phoneError = "Telefone inválido") }
            hasError = true
        }
        if (!CpfValidator.isValid(s.cpf)) {
            update { copy(cpfError = "CPF inválido") }
            hasError = true
        }
        val pwdResult = PasswordValidator.validate(s.password)
        if (!pwdResult.isValid) {
            update { copy(passwordError = pwdResult.errors.joinToString(", ")) }
            hasError = true
        }
        if (s.password != s.confirmPassword) {
            update { copy(confirmPasswordError = "Senhas não coincidem") }
            hasError = true
        }
        if (!s.termsAccepted) {
            update { copy(termsError = "Você precisa aceitar os termos para continuar") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            update { copy(isLoading = true, generalError = "") }
            val result = authRepository.registerUser(
                email = s.email.trim(),
                password = s.password,
                name = s.name.trim(),
                phone = s.phone,
                cpf = CpfValidator.strip(s.cpf)
            )
            result.fold(
                onSuccess = {
                    CredentialHolder.save(s.email.trim(), s.password)
                    update { copy(isLoading = false, navigateToVerify = true) }
                },
                onFailure = { error ->
                    update {
                        copy(
                            isLoading = false,
                            generalError = error.message ?: "Erro ao criar conta"
                        )
                    }
                }
            )
        }
    }

    private fun update(block: RegisterUserState.() -> RegisterUserState) {
        _state.value = _state.value.block()
    }
}

@Composable
fun RegisterUserScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVerify: () -> Unit,
    viewModel: RegisterUserViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(state.navigateToVerify) {
        if (state.navigateToVerify) {
            viewModel.onNavigationHandled()
            onNavigateToVerify()
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
            Spacer(Modifier.height(40.dp))
            RoleMatchLogo()
            Spacer(Modifier.height(24.dp))
            AccountTypeToggle(isRestaurant = false, onToggle = {})
            Spacer(Modifier.height(24.dp))

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
                        "Criar conta",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Junte-se e descubra os melhores restaurantes",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    RoleMatchTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Nome completo",
                        leadingIcon = Icons.Default.AccountCircle,
                        isError = state.nameError.isNotEmpty(),
                        errorMessage = state.nameError,
                        placeholder = "João Silva",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    RoleMatchTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "E-mail",
                        leadingIcon = Icons.Default.MailOutline,
                        isError = state.emailError.isNotEmpty(),
                        errorMessage = state.emailError,
                        placeholder = "seu@email.com",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(14.dp))
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
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    PasswordTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Senha",
                        isError = state.passwordError.isNotEmpty(),
                        errorMessage = state.passwordError,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    Spacer(Modifier.height(14.dp))
                    PasswordTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = "Confirmar senha",
                        isError = state.confirmPasswordError.isNotEmpty(),
                        errorMessage = state.confirmPasswordError,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() }
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = state.termsAccepted,
                            onCheckedChange = viewModel::onTermsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryGreen,
                                uncheckedColor = if (state.termsError.isNotEmpty())
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )) { append("Li e aceito os ") }
                                withStyle(SpanStyle(
                                    color = PrimaryGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )) { append("Termos de Uso") }
                                withStyle(SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )) { append(" e a ") }
                                withStyle(SpanStyle(
                                    color = PrimaryGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )) { append("Política de Privacidade") }
                            }
                        )
                    }
                    if (state.termsError.isNotEmpty()) {
                        Text(
                            text = state.termsError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                        )
                    }

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
                        "Criar conta",
                        viewModel::onRegister,
                        Modifier.fillMaxWidth(),
                        isLoading = state.isLoading
                    )

                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = onNavigateToLogin) {
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )) { append("Já tem uma conta? ") }
                                    withStyle(SpanStyle(
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )) { append("Faça login") }
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
        if (state.isLoading) LoadingOverlay("Criando sua conta...")
    }
}

// ─── Register Restaurant ──────────────────────────────────────────────────────

data class RegisterRestaurantState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val cnpj: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String = "",
    val emailError: String = "",
    val phoneError: String = "",
    val cnpjError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val termsError: String = "",
    val navigateToSetup: Boolean = false,
    val generalError: String = ""
)

class RegisterRestaurantViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterRestaurantState())
    val state: StateFlow<RegisterRestaurantState> = _state.asStateFlow()

    private val authRepository = RepositoryProvider.auth

    fun onNameChange(v: String) = update { copy(name = v, nameError = "") }
    fun onEmailChange(v: String) = update { copy(email = v, emailError = "") }

    fun onPhoneChange(v: String) {
        val digits = v.replace(Regex("[^0-9]"), "")
        update { copy(phone = PhoneValidator.format(digits), phoneError = "") }
    }

    fun onCnpjChange(v: String) {
        // Aceita letras e números — suporte ao CNPJ alfanumérico (IN RFB 2.229/2024)
        update { copy(cnpj = CnpjValidator.format(v), cnpjError = "") }
    }

    fun onPasswordChange(v: String) = update { copy(password = v, passwordError = "") }
    fun onConfirmPasswordChange(v: String) =
        update { copy(confirmPassword = v, confirmPasswordError = "") }
    fun onTermsChange(v: Boolean) = update { copy(termsAccepted = v, termsError = "") }
    fun onNavigationHandled() = update { copy(navigateToSetup = false) }

    fun onRegister() {
        val s = _state.value
        var hasError = false

        if (s.name.trim().length < 2) {
            update { copy(nameError = "Nome inválido") }
            hasError = true
        }
        if (!EmailValidator.isValid(s.email)) {
            update { copy(emailError = "E-mail inválido") }
            hasError = true
        }
        if (!PhoneValidator.isValid(s.phone)) {
            update { copy(phoneError = "Telefone inválido") }
            hasError = true
        }
        if (!CnpjValidator.isValid(s.cnpj)) {
            update { copy(cnpjError = "CNPJ inválido") }
            hasError = true
        }
        val pwdResult = PasswordValidator.validate(s.password)
        if (!pwdResult.isValid) {
            update { copy(passwordError = pwdResult.errors.joinToString(", ")) }
            hasError = true
        }
        if (s.password != s.confirmPassword) {
            update { copy(confirmPasswordError = "Senhas não coincidem") }
            hasError = true
        }
        if (!s.termsAccepted) {
            update { copy(termsError = "Você precisa aceitar os termos para continuar") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            update { copy(isLoading = true, generalError = "") }
            val result = authRepository.registerRestaurant(
                email = s.email.trim(),
                password = s.password,
                name = s.name.trim(),
                phone = s.phone,
                cnpj = CnpjValidator.strip(s.cnpj)
            )
            result.fold(
                onSuccess = {
                    CredentialHolder.save(s.email.trim(), s.password)
                    update { copy(isLoading = false, navigateToSetup = true) }
                },
                onFailure = { error ->
                    update {
                        copy(
                            isLoading = false,
                            generalError = error.message ?: "Erro ao criar conta"
                        )
                    }
                }
            )
        }
    }

    private fun update(block: RegisterRestaurantState.() -> RegisterRestaurantState) {
        _state.value = _state.value.block()
    }
}

@Composable
fun RegisterRestaurantScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSetup: () -> Unit,
    viewModel: RegisterRestaurantViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(state.navigateToSetup) {
        if (state.navigateToSetup) {
            viewModel.onNavigationHandled()
            onNavigateToSetup()
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
            Spacer(Modifier.height(40.dp))
            RoleMatchLogo()
            Spacer(Modifier.height(24.dp))
            AccountTypeToggle(isRestaurant = true, onToggle = {})
            Spacer(Modifier.height(24.dp))

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
                        "Criar conta",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Cadastre seu restaurante e alcance mais clientes",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    RoleMatchTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Nome do restaurante",
                        leadingIcon = Icons.Default.Storefront,
                        isError = state.nameError.isNotEmpty(),
                        errorMessage = state.nameError,
                        placeholder = "Restaurante XYZ",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    RoleMatchTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "E-mail",
                        leadingIcon = Icons.Default.MailOutline,
                        isError = state.emailError.isNotEmpty(),
                        errorMessage = state.emailError,
                        placeholder = "seu@email.com",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(14.dp))
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

                    // CNPJ — keyboardType = Text para suportar alfanumérico (IN RFB 2.229/2024)
                    RoleMatchTextField(
                        value = state.cnpj,
                        onValueChange = viewModel::onCnpjChange,
                        label = "CNPJ",
                        leadingIcon = Icons.Default.Apartment,
                        isError = state.cnpjError.isNotEmpty(),
                        errorMessage = state.cnpjError,
                        placeholder = "00.000.000/0000-00",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,  // Text para aceitar letras
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    PasswordTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Senha",
                        isError = state.passwordError.isNotEmpty(),
                        errorMessage = state.passwordError,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    Spacer(Modifier.height(14.dp))
                    PasswordTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = "Confirmar senha",
                        isError = state.confirmPasswordError.isNotEmpty(),
                        errorMessage = state.confirmPasswordError,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() }
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = state.termsAccepted,
                            onCheckedChange = viewModel::onTermsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryGreen,
                                uncheckedColor = if (state.termsError.isNotEmpty())
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )) { append("Li e aceito os ") }
                                withStyle(SpanStyle(
                                    color = PrimaryGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )) { append("Termos de Uso") }
                                withStyle(SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )) { append(" e a ") }
                                withStyle(SpanStyle(
                                    color = PrimaryGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )) { append("Política de Privacidade") }
                            }
                        )
                    }
                    if (state.termsError.isNotEmpty()) {
                        Text(
                            text = state.termsError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                        )
                    }

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
                        "Criar conta",
                        viewModel::onRegister,
                        Modifier.fillMaxWidth(),
                        isLoading = state.isLoading
                    )

                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = onNavigateToLogin) {
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )) { append("Já tem uma conta? ") }
                                    withStyle(SpanStyle(
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )) { append("Faça login") }
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
        if (state.isLoading) LoadingOverlay("Criando sua conta...")
    }
}