package com.pentagono.rolematch

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.pentagono.rolematch.ui.theme.RoleMatchTheme
import com.pentagono.rolematch.ui.screens.restaurant_setup.RestaurantSetupScreen

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RoleMatchTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    RestaurantSetupScreen()
                }
            }
        }
    }
}




