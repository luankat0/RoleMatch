package com.pentagono.rolematch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pentagono.rolematch.ui.components.BottomBar

@Composable
fun ProfileScreen(navController: NavController) {

    val notifications = remember { mutableStateOf(true) }
    val location = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Perfil", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Oi, Felipe Rocha", style = MaterialTheme.typography.titleLarge)
        Text("Membro Gourmet desde 2023")

        Spacer(modifier = Modifier.height(30.dp))

        Text("Configurações")

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notificações")
            Switch(
                checked = notifications.value,
                onCheckedChange = { notifications.value = it }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Localização")
            Switch(
                checked = location.value,
                onCheckedChange = { location.value = it }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { navController.navigate("preferences") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomBar(navController)
    }
}