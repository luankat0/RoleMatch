package com.pentagono.rolematch.ui.screens.restaurant_setup.steps

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pentagono.rolematch.viewmodel.RestaurantSetupViewModel

@Composable
fun PhotosStep(viewModel: RestaurantSetupViewModel) {
    val state by viewModel.state.collectAsState()

    Column {
        Text(
            "Fotos e Cardápio",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Seção de Fotos
        Text(
            "Fotos do restaurante",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFFF6A00),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Upload,
                    contentDescription = null,
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Adicionar", color = Color.Gray, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Adicione até 6 fotos do seu restaurante",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Seção de Cardápio
        Text(
            "Cardápio",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6A00)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Link", color = Color.White)
            }

            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("PDF")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.menuLink,
            onValueChange = {
                viewModel.update { copy(menuLink = it) }
            },
            placeholder = { Text("https://seu-cardapio-digital.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        )
    }
}