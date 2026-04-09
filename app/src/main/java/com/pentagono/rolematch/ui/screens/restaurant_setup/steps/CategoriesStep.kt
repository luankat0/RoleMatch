package com.pentagono.rolematch.ui.screens.restaurant_setup.steps

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import com.pentagono.rolematch.viewmodel.RestaurantSetupViewModel
import com.pentagono.rolematch.ui.components.SelectableChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesStep(viewModel: RestaurantSetupViewModel) {

    val state by viewModel.state.collectAsState()

    val establishmentTypes = listOf(
        "Restaurante", "Cafeteria", "Bar",
        "Pizzaria", "Bistrô", "Churrascaria"
    )

    val cuisineTypes = listOf(
        "Italiana", "Japonesa", "Brasileira",
        "Mexicana", "Francesa", "Árabe"
    )

    val vouchers = listOf("Alelo", "VR", "Sodexo", "Ticket")
    val prices = listOf("$", "$$", "$$$", "$$$$")

    Column {

        Text("Categorias e Características", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

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
                                    if (state.establishmentTypes.contains(type))
                                        state.establishmentTypes - type
                                    else
                                        state.establishmentTypes + type
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                                    if (state.cuisineTypes.contains(type))
                                        state.cuisineTypes - type
                                    else
                                        state.cuisineTypes + type
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Faixa de preço")
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prices.forEachIndexed { index, price ->
                val selected = state.priceRange == index + 1

                OutlinedButton(
                    onClick = {
                        viewModel.update {
                            copy(priceRange = index + 1)
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) Color(0xFFFF6A00) else Color.Transparent,
                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(price)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Vales refeição aceitos")
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
                                    if (state.mealVouchers.contains(voucher))
                                        state.mealVouchers - voucher
                                    else
                                        state.mealVouchers + voucher
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.petFriendly,
                onClick = {
                    viewModel.update {
                        copy(petFriendly = !state.petFriendly)
                    }
                },
                label = { Text("Pet Friendly") },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = state.kidsArea,
                onClick = {
                    viewModel.update {
                        copy(kidsArea = !state.kidsArea)
                    }
                },
                label = { Text("Área Kids") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}