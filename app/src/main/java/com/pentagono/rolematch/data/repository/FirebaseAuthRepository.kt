package com.pentagono.rolematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.domain.model.User
import com.pentagono.rolematch.domain.model.UserRole
import com.pentagono.rolematch.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun loginWithEmail(
        email: String,
        password: String
    ): Result<User> {
        return try {
            val cleanEmail = email.trim()

            val result = auth
                .signInWithEmailAndPassword(cleanEmail, password)
                .await()

            val firebaseUser = result.user
                ?: return Result.failure(Exception("Erro ao fazer login"))

            firebaseUser.reload().await()

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

            if (!firebaseUser.isEmailVerified) {
                auth.signOut()
                return Result.failure(
                    Exception("E-mail não verificado. Verifique sua caixa de entrada antes de entrar.")
                )
            }

            val user = User(
                id = firebaseUser.uid,
                name = userDoc.getString("name") ?: firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: cleanEmail,
                phone = userDoc.getString("phone") ?: "",
                cpf = userDoc.getString("cpf") ?: "",
                role = role,
                isVerified = firebaseUser.isEmailVerified
            )

            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("E-mail ou senha incorretos"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao fazer login"))
        }
    }

    // Retorna:
    // Result.success(user) com user.isVerified = true  → já tinha conta, vai para Home
    // Result.success(user) com user.isVerified = false → primeira vez, precisa completar perfil
    // Result.failure → erro
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Erro ao autenticar com Google"))

            // Verifica se já tem dados no Firestore (usuário já completou perfil)
            val userDoc = firestore
                .collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val isNewUser = !userDoc.exists() || userDoc.getString("phone").isNullOrEmpty()

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
                // isVerified = false sinaliza que precisa completar perfil
                isVerified = !isNewUser
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao fazer login com Google"))
        }
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        cpf: String
    ): Result<User> {
        return try {
            val cleanEmail = email.trim()

            val result = auth
                .createUserWithEmailAndPassword(cleanEmail, password)
                .await()

            val firebaseUser = result.user
                ?: return Result.failure(Exception("Erro ao criar conta"))

            firebaseUser.sendEmailVerification().await()

            val userData = hashMapOf(
                "name" to name,
                "email" to cleanEmail,
                "phone" to phone,
                "cpf" to cpf,
                "role" to "USER",
                "isVerified" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore
                .collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            auth.signOut()

            Result.success(
                User(
                    id = firebaseUser.uid,
                    name = name,
                    email = cleanEmail,
                    phone = phone,
                    cpf = cpf,
                    role = UserRole.USER,
                    isVerified = false
                )
            )
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Senha muito fraca. Use pelo menos 6 caracteres"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Este e-mail já está cadastrado"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao criar conta"))
        }
    }

    override suspend fun registerRestaurant(
        email: String,
        password: String,
        name: String,
        phone: String,
        cnpj: String
    ): Result<User> {
        return try {
            val cleanEmail = email.trim()

            val result = auth
                .createUserWithEmailAndPassword(cleanEmail, password)
                .await()

            val firebaseUser = result.user
                ?: return Result.failure(Exception("Erro ao criar conta"))

            firebaseUser.sendEmailVerification().await()

            val userData = hashMapOf(
                "name" to name,
                "email" to cleanEmail,
                "phone" to phone,
                "cnpj" to cnpj,
                "role" to "RESTAURANT",
                "isVerified" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore
                .collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            auth.signOut()

            Result.success(
                User(
                    id = firebaseUser.uid,
                    name = name,
                    email = cleanEmail,
                    phone = phone,
                    role = UserRole.RESTAURANT,
                    isVerified = false
                )
            )
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Senha muito fraca. Use pelo menos 6 caracteres"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Este e-mail já está cadastrado"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao criar conta"))
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            val cleanEmail = email.trim().lowercase()
            auth.sendPasswordResetEmail(cleanEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: "Erro ao enviar e-mail. Verifique o endereço digitado.")
            )
        }
    }

    override suspend fun sendVerificationCode(email: String): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser
                ?: return Result.failure(Exception("Nenhum usuário logado"))

            firebaseUser.reload().await()

            if (firebaseUser.isEmailVerified) {
                return Result.success(Unit)
            }

            firebaseUser.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao reenviar e-mail de verificação"))
        }
    }

    override suspend fun verifyCode(email: String, code: String): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser
                ?: return Result.failure(Exception("Nenhum usuário logado"))

            firebaseUser.reload().await()

            if (firebaseUser.isEmailVerified) {
                firestore
                    .collection("users")
                    .document(firebaseUser.uid)
                    .update("isVerified", true)
                    .await()
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("E-mail ainda não verificado. Clique no link enviado para seu e-mail.")
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao verificar e-mail"))
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            isVerified = firebaseUser.isEmailVerified
        )
    }

    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    name = it.displayName ?: "",
                    email = it.email ?: "",
                    isVerified = it.isEmailVerified
                )
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}