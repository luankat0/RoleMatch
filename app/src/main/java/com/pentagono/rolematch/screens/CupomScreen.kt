package com.pentagono.rolematch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. DADOS MOCADOS (A estrutura da informação)
data class CupomV2(
    val restaurante: String,
    val oferta: String,
    val detalhe: String,
    val validade: String,
    val destaqueLado: String // Ex: "20%" ou "Grátis"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupomScreen() {
    // 2. LISTA DE EXEMPLO (Mocada)
    val cupons = listOf(
        CupomV2("Trattoria Bella Vista", "20% OFF no jantar", "Válido de segunda a quinta", "30/05/2026", "20%"),
        CupomV2("Trattoria Bella Vista", "Sobremesa grátis", "Na compra de prato principal", "29/04/2026", "Grátis"),
        CupomV2("Burger King", "Combo Whopper", "Apenas no balcão", "15/05/2026", "Oferta"),
        CupomV2("Sushi House", "Temaki em dobro", "Válido para delivery", "10/06/2026", "2x1")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Cupons", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)), // Fundo levemente cinza
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cupons) { cupom ->
                CupomCardV2(cupom)
            }
        }
    }
}

// 3. COMPONENTE VISUAL (Baseado na sua nova referência)
@Composable
fun CupomCardV2(cupom: CupomV2) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min) // Faz a linha ter a altura do conteúdo
                .fillMaxWidth()
        ) {
            // Lado Esquerdo: Conteúdo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Círculo placeholder para a logo do restaurante
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(cupom.restaurante, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Ver restaurante", color = Color.Red, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(cupom.oferta, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(cupom.detalhe, color = Color.Gray, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(12.dp))
                Text("Válido até ${cupom.validade}", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Ação de resgate */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Resgatar Cupom", color = Color.White)
                }
            }

            // Lado Direito: Barra de Destaque (Vermelha)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                // Aquele detalhe do semicírculo branco lateral
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = (-40).dp) // Ajuste para parecer um recorte
                        .clip(CircleShape)
                        .background(Color.White)
                )

                // Texto na vertical (Rotação de 90 graus)
                Text(
                    text = cupom.destaqueLado,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.rotate(-90f) // Gira o texto para ficar vertical
                )
            }
        }
    }
}