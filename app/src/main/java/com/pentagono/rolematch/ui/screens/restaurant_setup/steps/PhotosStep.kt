package com.pentagono.rolematch.ui.screens.restaurant_setup.steps

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import com.pentagono.rolematch.viewmodel.RestaurantSetupViewModel

@Composable
fun PhotosStep(viewModel: RestaurantSetupViewModel) {

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Fotos e Cardápio")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // aqui depois você integra upload
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar fotos")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // finalizar
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finalizar")
        }
    }
}