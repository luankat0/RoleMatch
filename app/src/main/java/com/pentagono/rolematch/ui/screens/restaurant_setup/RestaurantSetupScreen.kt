package com.pentagono.rolematch.ui.screens.restaurant_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pentagono.rolematch.viewmodel.RestaurantSetupViewModel
import com.pentagono.rolematch.ui.screens.restaurant_setup.steps.*

@Composable
fun RestaurantSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: RestaurantSetupViewModel = viewModel()
) {
    val step by viewModel.step.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {

        // Header com título e progresso
        Column {
            Text(
                text = "Configurar Restaurante",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (index <= step) Color(0xFFFF6A00) else Color(0xFFE0E0E0),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Conteúdo scrollável (Card centralizado)
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                when (step) {
                    0 -> BasicInfoStep(viewModel)
                    1 -> CategoriesStep(viewModel)
                    2 -> PhotosStep(viewModel)
                    else -> BasicInfoStep(viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botões
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if (step > 0) {
                OutlinedButton(
                    onClick = { viewModel.previousStep() },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Voltar")
                }
            }

            Button(
                onClick = {
                    if (step < 2) {
                        viewModel.nextStep()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6A00)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(if (step > 0) 2f else 1f)
                    .height(48.dp)
            ) {
                Text(
                    if (step == 2) "Finalizar e Visualizar" else "Continuar",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}