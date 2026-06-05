package com.pentagono.rolematch.core.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pentagono.rolematch.core.theme.ThemeViewModel
import com.pentagono.rolematch.presentation.screens.admin.AdminCouponsScreen
import com.pentagono.rolematch.presentation.screens.admin.AdminDashboardScreen
import com.pentagono.rolematch.presentation.screens.admin.AdminReportsScreen
import com.pentagono.rolematch.presentation.screens.admin.AdminRestaurantsScreen
import com.pentagono.rolematch.presentation.screens.admin.AdminUsersScreen
import com.pentagono.rolematch.presentation.screens.auth.GoogleCompleteProfileScreen
import com.pentagono.rolematch.presentation.screens.auth.LoginScreen
import com.pentagono.rolematch.presentation.screens.auth.LoginViewModel
import com.pentagono.rolematch.presentation.screens.auth.RegisterRestaurantScreen
import com.pentagono.rolematch.presentation.screens.auth.RegisterUserScreen
import com.pentagono.rolematch.presentation.screens.auth.SplashScreen
import com.pentagono.rolematch.presentation.screens.auth.VerifyEmailScreen
import com.pentagono.rolematch.presentation.screens.auth.OnboardingScreen
import com.pentagono.rolematch.presentation.screens.auth.hasSeenOnboarding
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantCouponsScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantEditScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantReservationsScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantViewModel
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantDashboardScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantHomeScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantPreviewScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantProfileScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantQrValidatorScreen
import com.pentagono.rolematch.presentation.screens.restaurant.RestaurantSetupScreen
import com.pentagono.rolematch.presentation.screens.user.BestForEventsScreen
import com.pentagono.rolematch.presentation.screens.user.ChangePasswordScreen
import com.pentagono.rolematch.presentation.screens.user.CouponDetailsScreen
import com.pentagono.rolematch.presentation.screens.user.CouponQrCodeScreen
import com.pentagono.rolematch.presentation.screens.user.CouponScreen
import com.pentagono.rolematch.presentation.screens.user.CrowdFavoritesScreen
import com.pentagono.rolematch.presentation.screens.user.ForgotPasswordScreen
import com.pentagono.rolematch.presentation.screens.user.HelpCenterScreen
import com.pentagono.rolematch.presentation.screens.user.HomeScreen
import com.pentagono.rolematch.presentation.screens.user.HomeViewModel
import com.pentagono.rolematch.presentation.screens.user.MatchPreferencesScreen
import com.pentagono.rolematch.presentation.screens.user.MatchResultsScreen
import com.pentagono.rolematch.presentation.screens.user.MatchViewModel
import com.pentagono.rolematch.presentation.screens.user.MostFamousScreen
import com.pentagono.rolematch.presentation.screens.user.NearbyRestaurantsScreen
import com.pentagono.rolematch.presentation.screens.user.NotificationsScreen
import com.pentagono.rolematch.presentation.screens.user.PrivacyPolicyScreen
import com.pentagono.rolematch.presentation.screens.user.ProfileScreen
import com.pentagono.rolematch.presentation.screens.user.ReservationScreen
import com.pentagono.rolematch.presentation.screens.user.RestaurantDetailScreen
import com.pentagono.rolematch.presentation.screens.user.SearchScreen
import com.pentagono.rolematch.presentation.screens.user.TermsOfUseScreen
import com.pentagono.rolematch.presentation.screens.user.UserSettingsScreen
import com.pentagono.rolematch.presentation.screens.user.VerifyCodeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
    themeViewModel: ThemeViewModel? = null,
    homeViewModel: HomeViewModel,
    loginViewModel: LoginViewModel,
    restaurantViewModel: RestaurantViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val homeState by homeViewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToUserHome = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToRestaurantHome = {
                    navController.navigate(Routes.RESTAURANT_HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToUserHome = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRestaurantHome = {
                    navController.navigate(Routes.RESTAURANT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToAdminHome = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegisterUser = {
                    navController.navigate(Routes.REGISTER_USER)
                },
                onNavigateToRegisterRestaurant = {
                    navController.navigate(Routes.REGISTER_RESTAURANT)
                },
                onForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onNavigateToCompleteProfile = {
                    navController.navigate(Routes.GOOGLE_COMPLETE_PROFILE) {
                        popUpTo(Routes.LOGIN) { inclusive = false }
                    }
                },
                viewModel = loginViewModel
            )
        }

        composable(Routes.GOOGLE_COMPLETE_PROFILE) {
            GoogleCompleteProfileScreen(
                onCompleted = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER_USER) {
            RegisterUserScreen(
                onNavigateToLogin = { navController.navigateUp() },
                onNavigateToVerify = {
                    navController.navigate(Routes.verifyCode("user")) {
                        popUpTo(Routes.REGISTER_USER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER_RESTAURANT) {
            RegisterRestaurantScreen(
                onNavigateToLogin = { navController.navigateUp() },
                onNavigateToSetup = {
                    navController.navigate(Routes.verifyCode("restaurant")) {
                        popUpTo(Routes.REGISTER_RESTAURANT) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.VERIFY_CODE,
            arguments = listOf(navArgument("accountType") { type = NavType.StringType })
        ) { backStackEntry: NavBackStackEntry ->
            val accountType = backStackEntry.arguments?.getString("accountType") ?: "user"
            val context = LocalContext.current
            VerifyEmailScreen(
                accountType = accountType,
                onVerified = {
                    val destination = if (accountType == "restaurant") {
                        Routes.RESTAURANT_SETUP
                    } else if (!hasSeenOnboarding(context)) {
                        Routes.ONBOARDING
                    } else {
                        Routes.USER_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.navigateUp() },
                onEmailSent = {}
            )
        }

        composable(Routes.USER_HOME) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToMatch = { navController.navigate(Routes.MATCH) },
                onNavigateToCoupons = { navController.navigate(Routes.COUPONS) },
                onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                },
                viewModel = homeViewModel
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.navigateUp() },
                onNavigateToMostFamous = { navController.navigate(Routes.MOST_FAMOUS) },
                onNavigateToCrowdFavorites = { navController.navigate(Routes.CROWD_FAVORITES) },
                onNavigateToNearby = { navController.navigate(Routes.NEARBY) },
                onNavigateToBestForEvents = { navController.navigate(Routes.BEST_FOR_EVENTS) },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                },
                onNavigateToMatch = { navController.navigate(Routes.MATCH) },
                onNavigateToCoupons = { navController.navigate(Routes.COUPONS) },
                onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) }
            )
        }

        composable(Routes.MOST_FAMOUS) {
            MostFamousScreen(
                onBack = { navController.navigateUp() },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                }
            )
        }

        composable(Routes.CROWD_FAVORITES) {
            CrowdFavoritesScreen(
                onBack = { navController.navigateUp() },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                }
            )
        }

        composable(Routes.NEARBY) {
            NearbyRestaurantsScreen(
                onBack = { navController.navigateUp() },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                }
            )
        }

        composable(Routes.BEST_FOR_EVENTS) {
            BestForEventsScreen(
                onBack = { navController.navigateUp() },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                }
            )
        }

        composable(Routes.MATCH) {
            val matchViewModel: MatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            MatchPreferencesScreen(
                onBack = { navController.navigateUp() },
                onNavigateToResults = { navController.navigate(Routes.MATCH_RESULTS) },
                onNavigateToHome = { navController.navigate(Routes.USER_HOME) },
                onNavigateToCoupons = { navController.navigate(Routes.COUPONS) },
                onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) },
                viewModel = matchViewModel
            )
        }

        composable(Routes.MATCH_RESULTS) {
            val matchViewModel: MatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            MatchResultsScreen(
                onBack = { navController.navigateUp() },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                },
                viewModel = matchViewModel
            )
        }

        composable(
            route = Routes.RESTAURANT_DETAIL,
            arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
        ) { backStackEntry: NavBackStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
            RestaurantDetailScreen(
                restaurantId = restaurantId,
                onBack = { navController.navigateUp() },
                onNavigateToReservation = { id ->
                    navController.navigate(Routes.reservation(id))
                },
                onNavigateToCouponDetails = { id ->
                    navController.navigate(Routes.couponDetails(id))
                }
            )
        }

        composable(
            route = Routes.RESERVATION,
            arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
        ) { backStackEntry: NavBackStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
            ReservationScreen(
                restaurantId = restaurantId,
                onBack = { navController.navigateUp() },
                onConfirmed = { navController.navigateUp() }
            )
        }

        composable(Routes.COUPONS) {
            CouponScreen(
                onNavigateToHome = { navController.navigate(Routes.USER_HOME) },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToMatch = { navController.navigate(Routes.MATCH) },
                onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) },
                onNavigateToCouponDetails = { id ->
                    navController.navigate(Routes.couponDetails(id))
                },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                }
            )
        }

        composable(
            route = Routes.COUPON_DETAILS,
            arguments = listOf(navArgument("couponId") { type = NavType.StringType })
        ) { backStackEntry: NavBackStackEntry ->
            val couponId = backStackEntry.arguments?.getString("couponId") ?: ""
            CouponDetailsScreen(
                couponId = couponId,
                onBack = { navController.navigateUp() },
                onNavigateToQr = { uid -> navController.navigate(Routes.couponQr(uid)) },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                }
            )
        }

        composable(
            route = Routes.COUPON_QR,
            arguments = listOf(navArgument("userCouponId") { type = NavType.StringType })
        ) { backStackEntry: NavBackStackEntry ->
            val userCouponId = backStackEntry.arguments?.getString("userCouponId") ?: ""
            CouponQrCodeScreen(
                userCouponId = userCouponId,
                onBack = { navController.navigateUp() }
            )
        }

        composable(Routes.USER_PROFILE) {
            ProfileScreen(
                onNavigateToHome = { navController.navigate(Routes.USER_HOME) },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToMatch = { navController.navigate(Routes.MATCH) },
                onNavigateToCoupons = { navController.navigate(Routes.COUPONS) },
                onNavigateToSettings = { navController.navigate(Routes.USER_SETTINGS) },
                onNavigateToRestaurant = { id ->
                    navController.navigate(Routes.restaurantDetail(id))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                favoriteRestaurantIds = homeState.favorites
            )
        }

        composable(Routes.USER_SETTINGS) {
            val context = LocalContext.current
            UserSettingsScreen(
                onBack = { navController.navigateUp() },
                themeViewModel = themeViewModel,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToHelpCenter = {
                    navController.navigate(Routes.HELP_CENTER)
                },
                onNavigateToTerms = {
                    navController.navigate(Routes.TERMS_OF_USE)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Routes.PRIVACY_POLICY)
                },
                onNavigateToContact = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:contato@rolematch.com.br")
                        putExtra(Intent.EXTRA_SUBJECT, "Fale conosco - Rolê Match")
                    }
                    context.startActivity(intent)
                }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.HELP_CENTER) {
            HelpCenterScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.TERMS_OF_USE) {
            TermsOfUseScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RESTAURANT_SETUP) {
            RestaurantSetupScreen(
                onCancel = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCompleted = {
                    navController.navigate(Routes.RESTAURANT_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RESTAURANT_HOME) {
            RestaurantHomeScreen(
                onNavigateToDashboard = { navController.navigate(Routes.RESTAURANT_DASHBOARD) },
                onNavigateToCoupons = { navController.navigate(Routes.RESTAURANT_COUPONS) },
                onNavigateToProfile = { navController.navigate(Routes.RESTAURANT_PROFILE) },
                onNavigateToPreview = { navController.navigate(Routes.RESTAURANT_PREVIEW) },
                onNavigateToReservations = { navController.navigate(Routes.RESTAURANT_RESERVATIONS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                restaurantViewModel = restaurantViewModel
            )
        }

        composable(Routes.RESTAURANT_PREVIEW) {
            RestaurantPreviewScreen(
                onBack = { navController.navigateUp() },
                restaurantViewModel = restaurantViewModel
            )
        }

        composable(Routes.RESTAURANT_DASHBOARD) {
            RestaurantDashboardScreen(
                onNavigateToHome = { navController.navigate(Routes.RESTAURANT_HOME) },
                onNavigateToCoupons = { navController.navigate(Routes.RESTAURANT_COUPONS) },
                onNavigateToProfile = { navController.navigate(Routes.RESTAURANT_PROFILE) },
                restaurantViewModel = restaurantViewModel
            )
        }

        composable(Routes.RESTAURANT_COUPONS) {
            RestaurantCouponsScreen(
                onNavigateToHome = { navController.navigate(Routes.RESTAURANT_HOME) },
                onNavigateToDashboard = { navController.navigate(Routes.RESTAURANT_DASHBOARD) },
                onNavigateToProfile = { navController.navigate(Routes.RESTAURANT_PROFILE) }
            )
        }

        composable(Routes.RESTAURANT_QR_VALIDATOR) {
            RestaurantQrValidatorScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.RESTAURANT_PROFILE) {
            RestaurantProfileScreen(
                onNavigateToHome = { navController.navigate(Routes.RESTAURANT_HOME) },
                onNavigateToDashboard = { navController.navigate(Routes.RESTAURANT_DASHBOARD) },
                onNavigateToCoupons = { navController.navigate(Routes.RESTAURANT_COUPONS) },
                onNavigateToEdit = { navController.navigate(Routes.RESTAURANT_EDIT) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                restaurantViewModel = restaurantViewModel
            )
        }

        composable(Routes.RESTAURANT_EDIT) {
            RestaurantEditScreen(
                onBack = {
                    restaurantViewModel.loadRestaurant()
                    navController.navigateUp()
                }
            )
        }

        composable(Routes.RESTAURANT_RESERVATIONS) {
            RestaurantReservationsScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onNavigateToUsers = { navController.navigate(Routes.ADMIN_USERS) },
                onNavigateToRestaurants = { navController.navigate(Routes.ADMIN_RESTAURANTS) },
                onNavigateToCoupons = { navController.navigate(Routes.ADMIN_COUPONS) },
                onNavigateToReports = { navController.navigate(Routes.ADMIN_REPORTS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADMIN_USERS) {
            AdminUsersScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.ADMIN_RESTAURANTS) {
            AdminRestaurantsScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.ADMIN_COUPONS) {
            AdminCouponsScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.ADMIN_REPORTS) {
            AdminReportsScreen(onBack = { navController.navigateUp() })
        }
    }
}