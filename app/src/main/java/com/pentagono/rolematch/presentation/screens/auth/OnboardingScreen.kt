package com.pentagono.rolematch.presentation.screens.auth

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.DarkGreen
import com.pentagono.rolematch.core.theme.LightGreen
import com.pentagono.rolematch.core.theme.OrangeAccent
import com.pentagono.rolematch.core.theme.PrimaryGreen
import com.pentagono.rolematch.presentation.components.PrimaryButton
import com.pentagono.rolematch.presentation.components.SecondaryButton
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val iconBg: Color,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        icon = RMIcons.Star,
        iconBg = PrimaryGreen,
        title = "Bem-vindo ao Rolê Match!",
        description = "Encontre os melhores restaurantes da sua cidade de forma rápida e personalizada."
    ),
    OnboardingPage(
        icon = RMIcons.Search,
        iconBg = DarkGreen,
        title = "Busque e Explore",
        description = "Pesquise por nome, culinária ou localização. Filtre por avaliação, preço e muito mais."
    ),
    OnboardingPage(
        icon = RMIcons.Star,
        iconBg = OrangeAccent,
        title = "Match Perfeito",
        description = "Use o Match para encontrar restaurantes com base nas suas preferências do momento — tipo de rolê, distância, pets e muito mais."
    ),
    OnboardingPage(
        icon = RMIcons.LocalOffer,
        iconBg = PrimaryGreen,
        title = "Cupons Exclusivos",
        description = "Resgate cupons de desconto nos seus restaurantes favoritos e economize em cada rolê."
    ),
    OnboardingPage(
        icon = RMIcons.DateRange,
        iconBg = DarkGreen,
        title = "Reserve com Facilidade",
        description = "Faça reservas diretamente pelo app, escolha o horário e o número de pessoas sem complicação."
    )
)

fun hasSeenOnboarding(context: Context): Boolean {
    val prefs = context.getSharedPreferences("rolematch_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("seen_onboarding", false)
}

fun markOnboardingAsSeen(context: Context) {
    val prefs = context.getSharedPreferences("rolematch_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("seen_onboarding", true).apply()
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    markOnboardingAsSeen(context)
                    onFinish()
                }) {
                    Text(text = "Pular", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            Spacer(Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        label = "indicator_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) PrimaryGreen
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (isLastPage) {
                PrimaryButton(
                    text = "Começar agora!",
                    onClick = {
                        markOnboardingAsSeen(context)
                        onFinish()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    text = "Próximo",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                SecondaryButton(
                    text = "Anterior",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pagerState.currentPage > 0
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(LightGreen, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(page.iconBg, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = page.title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = page.description,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}