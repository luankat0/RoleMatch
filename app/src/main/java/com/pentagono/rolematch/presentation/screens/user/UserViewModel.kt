package com.pentagono.rolematch.presentation.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.domain.model.User
import com.pentagono.rolematch.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String = ""
)

class UserViewModel : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _state.value = UserState(isLoading = false, error = "Usuário não logado")
                return@launch
            }

            try {
                val userDoc = firestore
                    .collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                val role = try {
                    UserRole.valueOf(userDoc.getString("role") ?: "USER")
                } catch (e: IllegalArgumentException) {
                    UserRole.USER
                }

                val user = User(
                    id = firebaseUser.uid,
                    name = userDoc.getString("name") ?: firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    phone = userDoc.getString("phone") ?: "",
                    cpf = userDoc.getString("cpf") ?: "",
                    role = role,
                    isVerified = firebaseUser.isEmailVerified
                )

                _state.value = UserState(user = user, isLoading = false)
            } catch (e: Exception) {
                _state.value = UserState(
                    isLoading = false,
                    error = "Erro ao carregar dados do usuário"
                )
            }
        }
    }

    fun reload() {
        _state.value = UserState(isLoading = true)
        loadCurrentUser()
    }
}