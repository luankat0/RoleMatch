package com.pentagono.rolematch.core.common

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object ImageUploadHelper {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Upload de foto de perfil do usuário
    suspend fun uploadUserProfilePhoto(context: Context, uri: Uri): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não autenticado"))
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("Não foi possível ler a imagem"))

            val ref = storage.reference
                .child("users/$uid/profile.jpg")

            ref.putBytes(bytes).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload de foto do restaurante
    suspend fun uploadRestaurantPhoto(context: Context, uri: Uri, restaurantId: String): Result<String> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("Não foi possível ler a imagem"))

            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference
                .child("restaurants/$restaurantId/photos/$fileName.jpg")

            ref.putBytes(bytes).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove foto do restaurante pelo URL
    suspend fun deleteRestaurantPhoto(url: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(url).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}