package com.pentagono.rolematch.presentation.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.R
import com.pentagono.rolematch.core.theme.PrimaryGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToUserHome: () -> Unit,
    onNavigateToRestaurantHome: () -> Unit
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600))
        delay(1500)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            onNavigateToLogin()
            return@LaunchedEffect
        }

        currentUser.reload().await()
        if (!currentUser.isEmailVerified) {
            FirebaseAuth.getInstance().signOut()
            onNavigateToLogin()
            return@LaunchedEffect
        }

        try {
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val role = userDoc.getString("role") ?: "USER"

            when (role) {
                "RESTAURANT" -> onNavigateToRestaurantHome()
                "ADMIN"      -> onNavigateToLogin()
                else         -> onNavigateToUserHome()
            }
        } catch (e: Exception) {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rolematch_logo),
                contentDescription = "Logo Rolê Match",
                tint = PrimaryGreen,
                modifier = Modifier.size(88.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "ROLÊ MATCH",
                color = PrimaryGreen,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Encontre seu lugar ideal",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}