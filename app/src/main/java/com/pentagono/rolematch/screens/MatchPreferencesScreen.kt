package com.pentagono.rolematch.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pentagono.rolematch.ui.components.BottomBar
import com.pentagono.rolematch.ui.components.SelectableChip

@Composable
fun MatchPreferencesScreen(navController: NavController) {
    var distance by remember { mutableFloatStateOf(12f) }

    var dateNight by remember { mutableStateOf(false) }
    var reuniao by remember { mutableStateOf(false) }
    var aniversario by remember { mutableStateOf(false) }
    var casual by remember { mutableStateOf(false) }

    var wifi by remember { mutableStateOf(false) }
    var ar by remember { mutableStateOf(false) }
    var pet by remember { mutableStateOf(false) }
    var estacionamento by remember { mutableStateOf(false) }

    var italiana by remember { mutableStateOf(false) }
    var japonesa by remember { mutableStateOf(false) }
    var hamburguer by remember { mutableStateOf(false) }
    var mexicana by remember { mutableStateOf(false) }
    var vegana by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Selecione as suas preferências pro seu evento.",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Distância Máxima")

        Slider(
            value = distance,
            onValueChange = { distance = it },
            valueRange = 0f..20f
        )

        Text("${distance.toInt()} km")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Tipo de Evento")

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            SelectableChip("Date Night", dateNight) { dateNight = !dateNight }
            SelectableChip("Reunião", reuniao) { reuniao = !reuniao }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            SelectableChip("Aniversário", aniversario) { aniversario = !aniversario }
            SelectableChip("Casual", casual) { casual = !casual }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Condições")

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            SelectableChip("Wi-Fi Grátis", wifi) { wifi = !wifi }
            SelectableChip("Ar Condicionado", ar) { ar = !ar }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            SelectableChip("Pet Friendly", pet) { pet = !pet }
            SelectableChip("Estacionamento", estacionamento) { estacionamento = !estacionamento }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Tipo de Comida")

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            SelectableChip("Italiana", italiana) { italiana = !italiana }
            SelectableChip("Japonesa", japonesa) { japonesa = !japonesa }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            SelectableChip("Hamburgueria", hamburguer) { hamburguer = !hamburguer }
            SelectableChip("Mexicana", mexicana) { mexicana = !mexicana }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            SelectableChip("Vegana", vegana) { vegana = !vegana }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar Match")
        }

        Spacer(modifier = Modifier.height(20.dp))

        BottomBar(navController)
    }
}