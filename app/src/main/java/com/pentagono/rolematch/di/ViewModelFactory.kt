package com.pentagono.rolematch.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pentagono.rolematch.RoleMatchApplication

/**
 * Factory genérica que constrói ViewModels recebendo o AppComponent (Dagger).
 * Cada módulo de DI específico (ex: AuthModule, MatchModule) expõe os ViewModels
 * que precisa, e essa factory escolhe a função correta com base na classe pedida.
 */
class ViewModelFactory(
    private val appComponent: AppComponent
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
            ?: throw IllegalArgumentException("ViewModel não registrado: ${modelClass.name}")
        return creator(appComponent) as T
    }

    companion object {
        // Mapa de Class -> função que constrói o ViewModel usando o AppComponent.
        // Cada novo ViewModel migrado para Dagger precisa de uma entrada aqui.
        val creators: MutableMap<Class<out ViewModel>, (AppComponent) -> ViewModel> = mutableMapOf()
    }
}

/**
 * Composable helper: obtém (ou cria) um ViewModel usando o AppComponent da Application.
 * Uso: val viewModel: MatchViewModel = appViewModel { MatchViewModel(it.restaurantRepository()) }
 */
@Composable
inline fun <reified T : ViewModel> appViewModel(
    crossinline create: (AppComponent) -> T
): T {
    val context = LocalContext.current
    val app = context.applicationContext as RoleMatchApplication
    val owner = LocalViewModelStoreOwner.current
        ?: throw IllegalStateException("Nenhum ViewModelStoreOwner disponível")

    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            return create(app.appComponent) as VM
        }
    }

    return viewModel(
        modelClass = T::class.java,
        viewModelStoreOwner = owner,
        factory = factory
    )
}