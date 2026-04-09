package com.pentagono.rolematch.ui.screens.restaurant_setup.steps

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import com.pentagono.rolematch.viewmodel.RestaurantSetupViewModel
import com.pentagono.rolematch.ui.components.InputWithIcon

@Composable
fun BasicInfoStep(viewModel: RestaurantSetupViewModel) {
    val state by viewModel.state.collectAsState()

    Column {

        Text(
            "Informações Básicas",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📍 ENDEREÇO
        InputWithIcon(
            value = state.address,
            onChange = { viewModel.update { copy(address = it) } },
            label = "Endereço completo",
            icon = Icons.Default.LocationOn,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 📞 TELEFONE
        InputWithIcon(
            value = state.phone,
            onChange = { viewModel.update { copy(phone = it) } },
            label = "Telefone",
            icon = Icons.Default.Phone,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🌐 WEBSITE
        InputWithIcon(
            value = state.website,
            onChange = { viewModel.update { copy(website = it) } },
            label = "Website",
            icon = Icons.Default.Language,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 📸 INSTAGRAM
        InputWithIcon(
            value = state.instagram,
            onChange = { viewModel.update { copy(instagram = it) } },
            label = "Instagram",
            icon = Icons.Default.CameraAlt,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 👍 FACEBOOK
        InputWithIcon(
            value = state.facebook,
            onChange = { viewModel.update { copy(facebook = it) } },
            label = "Facebook",
            icon = Icons.Default.ThumbUp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

// 📝 DESCRIÇÃO / HISTÓRIA
        OutlinedTextField(
            value = state.description,
            onValueChange = {
                viewModel.update { copy(description = it) }
            },
            label = { Text("História do restaurante") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.nextStep() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6A00)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }
    }
}