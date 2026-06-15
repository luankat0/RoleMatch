package com.pentagono.rolematch

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.pentagono.rolematch.core.navigation.AppNavGraph
import com.pentagono.rolematch.core.theme.RoleMatchTheme
import com.pentagono.rolematch.core.theme.ThemeViewModel
import com.pentagono.rolematch.di.AppViewModelFactory
import com.pentagono.rolematch.presentation.screens.auth.LoginViewModel
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantViewModel
import com.pentagono.rolematch.presentation.screens.user.HomeViewModel

class MainActivity : ComponentActivity() {

    // LoginViewModel agora usa @Inject constructor; precisa da AppViewModelFactory
    // (criada com o appComponent da Application) para o by viewModels() funcionar.
    private val loginViewModel: LoginViewModel by viewModels {
        AppViewModelFactory((application as RoleMatchApplication).appComponent)
    }

    // Sem dependências do Dagger — continuam com construtor padrão
    private val themeViewModel: ThemeViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val restaurantViewModel: RestaurantViewModel by viewModels()

    // Launcher para o Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                loginViewModel.onGoogleSignInResult(idToken)
            } else {
                loginViewModel.onGoogleSignInError("Token não encontrado")
            }
        } catch (e: ApiException) {
            loginViewModel.onGoogleSignInError("Erro ao autenticar com Google: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        // Configura o Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Passa a função de launch para o ViewModel
        loginViewModel.setGoogleSignInLauncher {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

            RoleMatchTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    homeViewModel = homeViewModel,
                    loginViewModel = loginViewModel,
                    restaurantViewModel = restaurantViewModel
                )
            }
        }
    }
}