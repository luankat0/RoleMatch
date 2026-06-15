package com.pentagono.rolematch.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.presentation.components.*

@Composable
fun LoginScreen(
    onNavigateToUserHome: () -> Unit,
    onNavigateToRestaurantHome: () -> Unit,
    onNavigateToAdminHome: () -> Unit = {},
    onNavigateToRegisterUser: () -> Unit,
    onNavigateToRegisterRestaurant: () -> Unit,
    onForgotPassword: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit = {},
    viewModel: LoginViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.navigateToUserHome) {
        if (state.navigateToUserHome) {
            viewModel.onNavigationHandled()
            onNavigateToUserHome()
        }
    }

    LaunchedEffect(state.navigateToAdminHome) {
        if (state.navigateToAdminHome) {
            viewModel.onNavigationHandled()
            onNavigateToAdminHome()
        }
    }

    LaunchedEffect(state.navigateToRestaurantHome) {
        if (state.navigateToRestaurantHome) {
            viewModel.onNavigationHandled()
            onNavigateToRestaurantHome()
        }
    }

    LaunchedEffect(state.navigateToCompleteProfile) {
        if (state.navigateToCompleteProfile) {
            viewModel.onNavigationHandled()
            onNavigateToCompleteProfile()
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
            Spacer(modifier = Modifier.height(48.dp))

            RoleMatchLogo()

            Spacer(modifier = Modifier.height(28.dp))

            AccountTypeToggle(
                isRestaurant = state.isRestaurant,
                onToggle = viewModel::onAccountTypeChange
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text(
                        text = "Bem-vindo de volta!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (state.isRestaurant) {
                            "Acesse o painel do seu restaurante"
                        } else {
                            "Faça login para descobrir restaurantes incríveis"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    RoleMatchTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "E-mail",
                        leadingIcon = RMIcons.Email,
                        isError = state.emailError.isNotEmpty(),
                        errorMessage = state.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        placeholder = "seu@email.com"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Senha",
                        isError = state.passwordError.isNotEmpty(),
                        errorMessage = state.passwordError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = state.rememberMe,
                                onCheckedChange = viewModel::onRememberMeChange,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryGreen
                                )
                            )
                            Text(
                                text = "Lembrar-me",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(onClick = onForgotPassword) {
                            Text(
                                text = "Esqueceu a senha?",
                                color = PrimaryGreen,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.error.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    PrimaryButton(
                        text = "Entrar",
                        onClick = viewModel::onLogin,
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Divisor "ou" — só para usuários (não restaurante)
                    if (!state.isRestaurant) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                text = "  ou  ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botão Google
                        OutlinedButton(
                            onClick = viewModel::onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            // G colorido em texto (sem imagem externa)
                            Text(
                                text = "G",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Continuar com Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                if (state.isRestaurant) {
                                    onNavigateToRegisterRestaurant()
                                } else {
                                    onNavigateToRegisterUser()
                                }
                            }
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        append("Não tem uma conta? ")
                                    }
                                    withStyle(
                                        SpanStyle(
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("Cadastre-se")
                                    }
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}