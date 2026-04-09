package com.pentagono.rolematch.ui.screens.restaurant_setup.steps

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import com.pentagono.rolematch.viewmodel.RestaurantSetupViewModel
import com.pentagono.rolematch.ui.components.InputWithIcon

@Composable
fun BasicInfoStep(viewModel: RestaurantSetupViewModel) {

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text("Informações Básicas", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        InputWithIcon(
            value = state.address,
            onChange = { viewModel.update { copy(address = it) } },
            label = "Endereço completo",
            icon = Icons.Default.LocationOn,
            placeholder = "Rua, número, bairro, cidade",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        InputWithIcon(
            value = state.phone,
            onChange = { viewModel.update { copy(phone = it) } },
            label = "Telefone",
            icon = Icons.Default.Phone,
            placeholder = "(11) 98765-4321",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        InputWithIcon(
            value = state.website,
            onChange = { viewModel.update { copy(website = it) } },
            label = "Website (opcional)",
            icon = Icons.Default.Language,
            placeholder = "www.seurestaurante.com.br",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        InputWithIcon(
            value = state.instagram,
            onChange = { viewModel.update { copy(instagram = it) } },
            label = "Instagram (opcional)",
            icon = Icons.Default.CameraAlt,
            placeholder = "@seurestaurante",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        InputWithIcon(
            value = state.facebook,
            onChange = { viewModel.update { copy(facebook = it) } },
            label = "Facebook (opcional)",
            icon = Icons.Default.ThumbUp,
            placeholder = "SeuRestaurante",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = {
                viewModel.update { copy(description = it) }
            },
            label = { Text("Descrição/História do restaurante") },
            placeholder = { Text("Conte um pouco sobre a história do seu restaurante...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )
    }
}