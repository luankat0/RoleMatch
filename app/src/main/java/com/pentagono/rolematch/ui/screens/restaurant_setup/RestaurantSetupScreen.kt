package com.pentagono.rolematch.ui.screens.restaurant_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    viewModel: RestaurantSetupViewModel = viewModel()
) {
    val step by viewModel.step.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {

        Text(
            text = "Configurar Restaurante",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = (step + 1) / 3f,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFF6A00)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (step) {
                    0 -> BasicInfoStep(viewModel)
                    1 -> CategoriesStep(viewModel)
                    2 -> PhotosStep(viewModel)
                }
            }
        }
    }
}