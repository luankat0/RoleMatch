package com.pentagono.rolematch.ui.screens.restaurant_setup.steps

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import com.pentagono.rolematch.viewmodel.RestaurantSetupViewModel
import com.pentagono.rolematch.ui.components.SelectableChip
import com.pentagono.rolematch.ui.components.PrimaryButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesStep(viewModel: RestaurantSetupViewModel) {

    val state by viewModel.state.collectAsState()

    val establishmentTypes = listOf(
        "Restaurante", "Bar", "Cafeteria",
        "Pizzaria", "Bistrô", "Churrascaria"
    )

    val cuisineTypes = listOf(
        "Italiana", "Japonesa", "Brasileira",
        "Mexicana", "Francesa", "Árabe"
    )

    val vouchers = listOf(
        "Alelo", "VR", "Sodexo", "Ticket"
    )

    val prices = listOf("$", "$$", "$$$", "$$$$")

    Column(modifier = Modifier.padding(16.dp)) {

        // 🔙 BOTÃO VOLTAR
        TextButton(onClick = { viewModel.previousStep() }) {
            Text("← Voltar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🏪 TIPO DE ESTABELECIMENTO
        Text("Tipo de estabelecimento")

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            establishmentTypes.forEach { type ->
                SelectableChip(
                    label = type,
                    selected = state.establishmentTypes.contains(type),
                    onClick = {
                        viewModel.update {
                            copy(
                                establishmentTypes =
                                    if (establishmentTypes.contains(type))
                                        establishmentTypes - type
                                    else
                                        establishmentTypes + type
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🍝 TIPO DE COZINHA
        Text("Tipo de cozinha")

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cuisineTypes.forEach { type ->
                SelectableChip(
                    label = type,
                    selected = state.cuisineTypes.contains(type),
                    onClick = {
                        viewModel.update {
                            copy(
                                cuisineTypes =
                                    if (cuisineTypes.contains(type))
                                        cuisineTypes - type
                                    else
                                        cuisineTypes + type
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 💲 FAIXA DE PREÇO
        Text("Faixa de preço")

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            prices.forEachIndexed { index, price ->
                Button(
                    onClick = {
                        viewModel.update {
                            copy(priceRange = index + 1)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (state.priceRange == index + 1)
                                Color(0xFFFF6A00)
                            else Color.LightGray
                    )
                ) {
                    Text(price)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🎫 VALE REFEIÇÃO
        Text("Vale refeição aceitos")

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vouchers.forEach { voucher ->
                SelectableChip(
                    label = voucher,
                    selected = state.mealVouchers.contains(voucher),
                    onClick = {
                        viewModel.update {
                            copy(
                                mealVouchers =
                                    if (mealVouchers.contains(voucher))
                                        mealVouchers - voucher
                                    else
                                        mealVouchers + voucher
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🐶 👶 FEATURES
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            FilterChip(
                selected = state.petFriendly,
                onClick = {
                    viewModel.update {
                        copy(petFriendly = !petFriendly)
                    }
                },
                label = { Text("Pet Friendly") }
            )

            FilterChip(
                selected = state.kidsArea,
                onClick = {
                    viewModel.update {
                        copy(kidsArea = !kidsArea)
                    }
                },
                label = { Text("Área Kids") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ➡️ CONTINUAR
        PrimaryButton(
            text = "Continuar",
            onClick = { viewModel.nextStep() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}