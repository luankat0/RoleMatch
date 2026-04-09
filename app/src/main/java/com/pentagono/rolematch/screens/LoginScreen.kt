package com.pentagono.rolematch.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BackgroundCream = Color(0xFFF8F5EB)
val CardYellow = Color(0xFFFDF7DD)
val OliveGreen = Color(0xFF425A0F)
val DarkBrown = Color(0xFF261E0D)
val BlueText = Color(0xFF385596)
val ToggleBackground = Color(0xFFEEDFA4)
val DividerGray = Color(0xFFDCD6C2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var isClient by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo e título
        Row(verticalAlignment = Alignment.CenterVertically) {
//            Icon(
//                //imageVector = Icons.Default.Restaurant,
//                contentDescription = "Logo",
//                tint = OliveGreen,
//                modifier = Modifier.size(36.dp)
//            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Rolê Match",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OliveGreen
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sua jornada gastronômica começa aqui.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card do Formulário
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardYellow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Toggle Cliente / Restaurante
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(ToggleBackground, RoundedCornerShape(25.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isClient) OliveGreen else Color.Transparent,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { isClient = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cliente",
                            color = if (isClient) Color.White else OliveGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (!isClient) OliveGreen else Color.Transparent,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { isClient = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Restaurante",
                            color = if (!isClient) Color.White else OliveGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Social
                Text(text = "ENTRAR COM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OliveGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Botão Google - MockVisual
                    Box(
                       modifier = Modifier
                           .size(50.dp)
                           .clip(CircleShape)
                           .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        //Icon(Icons.Default.Email, contentDescription = "Google", tint = BlueText)
                    }
                    // Botão Facebook - MockVisual
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(BlueText),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("f", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divisor
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DividerGray)
                    Text(
                        text = "OU USE SEU E-MAIL",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DividerGray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Campo E-mail
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Email", color = OliveGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("exemplo@email.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = OliveGreen,
                            unfocusedBorderColor = DividerGray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Senha
                Column(modifier = Modifier.height(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Senha", color = OliveGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Esqueceu a senha?", color = BlueText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = OliveGreen,
                            unfocusedBorderColor = DividerGray
                        ),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                        trailingIcon = {
//                            val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
//                            IconButton(onClick = { passwordVisible = !passwordVisible}) {
//                                Icon(imageVector = image, contentDescription = "Mostrar senha", tint = Color.Gray)
//                            }
//                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botão Entrar com Mock de Rota
                Button(
                    onClick = {
                        // MOCK DE NAVEGAÇÃO / API
                        println("Chamando API de Login para: $email")
                        if (isClient) {
                            println("Logado com sucesso. Navegando para: Home")
                            // TODO: navController.navigate("home_cliente")
                        } else {
                            println("Logado com sucesso. Navegando para: Home do Restaurante")
                            // TODO: navController.navigate("home_restaurante")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBrown),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Entrar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cadastro
                Text(
                    text = buildAnnotatedString {
                        append("Não tem conta? ")
                        withStyle(style = SpanStyle(color = OliveGreen, fontWeight = FontWeight.Bold)) {
                            append("Cadastre-se")
                        }
                    },
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
