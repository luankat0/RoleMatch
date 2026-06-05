package com.pentagono.rolematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pentagono.rolematch.domain.model.Reservation
import com.pentagono.rolematch.domain.model.ReservationStatus
import com.pentagono.rolematch.domain.repository.ReservationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseReservationRepository : ReservationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = firestore.collection("reservations")

    override suspend fun createReservation(reservation: Reservation): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não autenticado"))

            val data = mapOf(
                "userId" to uid,
                "restaurantId" to reservation.restaurantId,
                "restaurantName" to reservation.restaurantName,
                "date" to reservation.date,
                "time" to reservation.time,
                "peopleCount" to reservation.peopleCount,
                "occasionType" to reservation.occasionType,
                "customerName" to reservation.customerName,
                "phone" to reservation.phone,
                "email" to reservation.email,
                "notes" to reservation.notes,
                "status" to ReservationStatus.CONFIRMED.name,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val docRef = collection.document()
            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Result<Unit> {
        return try {
            collection.document(reservationId).update("status", status.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserReservations(userId: String): Result<List<Reservation>> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val sorted = snapshot.documents.mapNotNull { it.toReservation() }
                .sortedByDescending { it.createdAt }
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRestaurantReservations(
        restaurantId: String,
        date: String?
    ): Result<List<Reservation>> {
        return try {
            var query = collection.whereEqualTo("restaurantId", restaurantId)
            if (date != null) {
                query = query.whereEqualTo("date", date)
            }
            val snapshot = query
                .get()
                .await()
            val sorted2 = snapshot.documents.mapNotNull { it.toReservation() }
                .sortedBy { it.time }
            Result.success(sorted2)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeReservations(restaurantId: String): Flow<List<Reservation>> = callbackFlow {
        val listener = collection
            .whereEqualTo("restaurantId", restaurantId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { it.toReservation() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReservation(): Reservation? {
        return try {
            val statusStr = getString("status") ?: "CONFIRMED"
            val status = try { ReservationStatus.valueOf(statusStr) }
            catch (e: Exception) { ReservationStatus.CONFIRMED }

            Reservation(
                id = id,
                userId = getString("userId") ?: "",
                restaurantId = getString("restaurantId") ?: "",
                restaurantName = getString("restaurantName") ?: "",
                date = getString("date") ?: "",
                time = getString("time") ?: "",
                peopleCount = (getLong("peopleCount") ?: 2L).toInt(),
                occasionType = getString("occasionType") ?: "",
                customerName = getString("customerName") ?: "",
                phone = getString("phone") ?: "",
                email = getString("email") ?: "",
                notes = getString("notes") ?: "",
                status = status
            )
        } catch (e: Exception) { null }
    }
}