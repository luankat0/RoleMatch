package com.pentagono.rolematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.pentagono.rolematch.domain.model.*
import com.pentagono.rolematch.domain.repository.RestaurantRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRestaurantRepository : RestaurantRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = firestore.collection("restaurants")

    override suspend fun getRestaurantById(id: String): Result<Restaurant> {
        return try {
            val doc = collection.document(id).get().await()
            val restaurant = doc.toRestaurant()
                ?: return Result.failure(Exception("Restaurante não encontrado"))
            Result.success(restaurant)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRestaurantByOwnerId(ownerId: String): Result<Restaurant> {
        return try {
            val query = collection.whereEqualTo("ownerId", ownerId).limit(1).get().await()
            val doc = query.documents.firstOrNull()
                ?: return Result.failure(Exception("Restaurante não encontrado"))
            val restaurant = doc.toRestaurant()
                ?: return Result.failure(Exception("Erro ao carregar restaurante"))
            Result.success(restaurant)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRestaurant(restaurant: Restaurant): Result<String> {
        return try {
            val ownerId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não autenticado"))

            val data = restaurant.toMap(ownerId)
            val docRef = if (restaurant.id.isNotEmpty()) {
                collection.document(restaurant.id)
            } else {
                collection.document()
            }
            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRestaurant(restaurant: Restaurant): Result<Unit> {
        return try {
            val data = restaurant.toMap(restaurant.ownerId)
            collection.document(restaurant.id).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchRestaurants(query: String): Result<List<Restaurant>> {
        return try {
            val snapshot = collection
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            val results = snapshot.documents
                .mapNotNull { it.toRestaurant() }
                .filter { r ->
                    r.name.contains(query, ignoreCase = true) ||
                            r.cuisineTypes.any { it.contains(query, ignoreCase = true) } ||
                            r.establishmentTypes.any { it.contains(query, ignoreCase = true) }
                }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNearbyRestaurants(
        lat: Double,
        lng: Double,
        radiusKm: Float
    ): Result<List<Restaurant>> {
        return try {
            val snapshot = collection
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            val results = snapshot.documents.mapNotNull { it.toRestaurant() }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFeaturedRestaurants(): Result<List<Restaurant>> {
        return try {
            val snapshot = collection
                .whereEqualTo("status", "ACTIVE")
                .orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            val results = snapshot.documents.mapNotNull { it.toRestaurant() }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRestaurant(id: String): Flow<Restaurant?> = callbackFlow {
        val listener = collection.document(id).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toRestaurant())
        }
        awaitClose { listener.remove() }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toRestaurant(): Restaurant? {
        return try {
            val address = (get("address") as? Map<*, *>)?.let { a ->
                Address(
                    cep = a["cep"] as? String ?: "",
                    street = a["street"] as? String ?: "",
                    number = a["number"] as? String ?: "",
                    neighborhood = a["neighborhood"] as? String ?: "",
                    city = a["city"] as? String ?: "",
                    state = a["state"] as? String ?: "",
                    complement = a["complement"] as? String ?: ""
                )
            } ?: Address()

            val features = (get("features") as? Map<*, *>)?.let { f ->
                RestaurantFeatures(
                    petFriendly = f["petFriendly"] as? Boolean ?: false,
                    kidsArea = f["kidsArea"] as? Boolean ?: false,
                    accessiblePcd = f["accessiblePcd"] as? Boolean ?: false,
                    acceptsVr = f["acceptsVr"] as? Boolean ?: false,
                    wifi = f["wifi"] as? Boolean ?: false,
                    parking = f["parking"] as? Boolean ?: false,
                    airConditioning = f["airConditioning"] as? Boolean ?: false
                )
            } ?: RestaurantFeatures()

            val statusStr = getString("status") ?: "PENDING_REVIEW"
            val status = try { RestaurantStatus.valueOf(statusStr) }
            catch (e: Exception) { RestaurantStatus.PENDING_REVIEW }

            Restaurant(
                id = id,
                ownerId = getString("ownerId") ?: "",
                name = getString("name") ?: "",
                legalName = getString("legalName") ?: "",
                cnpj = getString("cnpj") ?: "",
                description = getString("description") ?: "",
                category = getString("category") ?: "",
                cuisineTypes = (get("cuisineTypes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                establishmentTypes = (get("establishmentTypes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                address = address,
                phone = getString("phone") ?: "",
                whatsapp = getString("whatsapp") ?: "",
                website = getString("website") ?: "",
                instagram = getString("instagram") ?: "",
                priceRange = (getLong("priceRange") ?: 2L).toInt(),
                features = features,
                photos = (get("photos") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                menuLink = getString("menuLink") ?: "",
                acceptsReservations = getBoolean("acceptsReservations") ?: true,
                mealVouchers = (get("mealVouchers") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                status = status,
                rating = (getDouble("rating") ?: 0.0).toFloat(),
                reviewCount = (getLong("reviewCount") ?: 0L).toInt(),
                distanceKm = (getDouble("distanceKm") ?: 0.0).toFloat(),
                isOpen = getBoolean("isOpen") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Restaurant.toMap(ownerId: String): Map<String, Any?> {
        return mapOf(
            "ownerId" to ownerId,
            "name" to name,
            "legalName" to legalName,
            "cnpj" to cnpj,
            "description" to description,
            "category" to category,
            "cuisineTypes" to cuisineTypes,
            "establishmentTypes" to establishmentTypes,
            "address" to mapOf(
                "cep" to address.cep,
                "street" to address.street,
                "number" to address.number,
                "neighborhood" to address.neighborhood,
                "city" to address.city,
                "state" to address.state,
                "complement" to address.complement
            ),
            "phone" to phone,
            "whatsapp" to whatsapp,
            "website" to website,
            "instagram" to instagram,
            "priceRange" to priceRange,
            "features" to mapOf(
                "petFriendly" to features.petFriendly,
                "kidsArea" to features.kidsArea,
                "accessiblePcd" to features.accessiblePcd,
                "acceptsVr" to features.acceptsVr,
                "wifi" to features.wifi,
                "parking" to features.parking,
                "airConditioning" to features.airConditioning
            ),
            "photos" to photos,
            "menuLink" to menuLink,
            "acceptsReservations" to acceptsReservations,
            "mealVouchers" to mealVouchers,
            "status" to status.name,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "isOpen" to isOpen,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}