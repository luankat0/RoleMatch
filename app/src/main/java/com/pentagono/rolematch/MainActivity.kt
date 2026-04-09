package com.pentagono.rolematch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pentagono.rolematch.screens.CupomScreen
import com.pentagono.rolematch.screens.MatchPreferencesScreen
import com.pentagono.rolematch.screens.ProfileScreen
import com.pentagono.rolematch.ui.components.BottomBar
import com.pentagono.rolematch.ui.screens.restaurant_setup.RestaurantSetupScreen
import com.pentagono.rolematch.ui.theme.RoleMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RoleMatchTheme {
                val navController = rememberNavController()
                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute == "restaurant_setup") {
                            BottomBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "preferences",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("preferences") {
                            MatchPreferencesScreen(navController)
                        }

                        composable("profile") {
                            ProfileScreen(navController)
                        }

                        composable("coupons") {
                            CupomScreen(navController)
                        }

                        composable("restaurant_setup") {
                            RestaurantSetupScreen()
                        }
                    }
                }
            }
        }
    }
}