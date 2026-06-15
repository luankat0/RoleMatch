package com.pentagono.rolematch.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pentagono.rolematch.presentation.screens.auth.LoginViewModel

/**
 * Factory usada com 'by viewModels()' na MainActivity, apenas para ViewModels
 * que possuem construtor @Inject com dependências do AppComponent (Dagger).
 *
 * ThemeViewModel, HomeViewModel e RestaurantViewModel não usam RepositoryProvider
 * (usam Firebase direto) e continuam com 'by viewModels()' padrão, sem essa factory.
 */
class AppViewModelFactory(
    private val appComponent: AppComponent
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            LoginViewModel::class.java ->
                LoginViewModel(appComponent.authRepository()) as T

            else -> throw IllegalArgumentException("ViewModel não registrado na AppViewModelFactory: ${modelClass.name}")
        }
    }
}