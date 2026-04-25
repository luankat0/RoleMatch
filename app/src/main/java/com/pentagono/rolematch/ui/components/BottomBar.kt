package com.pentagono.rolematch.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(navController: NavController) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "restaurant_setup",
            onClick = {
                navController.navigate("restaurant_setup") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == "preferences",
            onClick = {
                navController.navigate("preferences") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar") },
            label = { Text("Search") }
        )

        NavigationBarItem(
            selected = currentRoute == "preferences",
            onClick = {
                navController.navigate("preferences") {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    Icons.Outlined.ConfirmationNumber,
                    contentDescription = "Match"
                )
            },
            label = { Text("Match") }
        )

        NavigationBarItem(
            selected = currentRoute == "coupons",
            onClick = {
                navController.navigate("coupons") {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    Icons.Outlined.ConfirmationNumber,
                    contentDescription = "Cupons"
                )
            },
            label = { Text("Coupons") }
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
            label = { Text("Profile") }
        )
    }
}