package com.pentagono.rolematch.data.repository

import com.pentagono.rolematch.core.common.MockData
import com.pentagono.rolematch.domain.model.*
import com.pentagono.rolematch.domain.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class MockCouponRepository : CouponRepository {
    private val userCoupons = mutableListOf<UserCoupon>()

    override suspend fun getCouponById(id: String): Result<Coupon> {
        return MockData.coupons.find { it.id == id }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Cupom não encontrado"))
    }

    override suspend fun getCouponsByRestaurant(restaurantId: String): Result<List<Coupon>> =
        Result.success(MockData.getCouponsForRestaurant(restaurantId))

    override suspend fun getCouponsWithAvailability(): Result<List<Coupon>> =
        Result.success(MockData.coupons.filter { it.active })

    override suspend fun createCoupon(coupon: Coupon): Result<String> {
        delay(400)
        return Result.success(UUID.randomUUID().toString())
    }

    override suspend fun updateCoupon(coupon: Coupon): Result<Unit> = Result.success(Unit)

    override suspend fun deleteCoupon(couponId: String): Result<Unit> = Result.success(Unit)

    override suspend fun issueCouponToUser(couponId: String, userId: String): Result<UserCoupon> {
        delay(500)
        val coupon = MockData.coupons.find { it.id == couponId }
            ?: return Result.failure(Exception("Cupom não encontrado"))
        val userCoupon = UserCoupon(
            id = UUID.randomUUID().toString(),
            couponId = couponId,
            userId = userId,
            restaurantId = coupon.restaurantId,
            couponTitle = coupon.title,
            discountValue = coupon.discountValue,
            qrToken = generateQrToken(couponId, userId),
            status = CouponStatus.AVAILABLE,
            issuedAt = java.util.Date(),
            expiresAt = coupon.validUntil
        )
        userCoupons.add(userCoupon)
        return Result.success(userCoupon)
    }

    override suspend fun validateCouponQr(qrToken: String, restaurantId: String): Result<UserCoupon> {
        delay(600)
        val uc = userCoupons.find { it.qrToken == qrToken }
            ?: return Result.failure(Exception("QR Code inválido"))
        if (uc.restaurantId != restaurantId)
            return Result.failure(Exception("Restaurante incorreto"))
        if (uc.status == CouponStatus.USED)
            return Result.failure(Exception("Cupom já utilizado"))
        if (uc.expiresAt.before(java.util.Date()))
            return Result.failure(Exception("Cupom expirado"))
        return Result.success(uc)
    }

    override suspend fun markCouponAsUsed(userCouponId: String, operatorId: String): Result<Unit> {
        val idx = userCoupons.indexOfFirst { it.id == userCouponId }
        if (idx >= 0) {
            userCoupons[idx] = userCoupons[idx].copy(
                status = CouponStatus.USED,
                usedAt = java.util.Date(),
                validatedBy = operatorId
            )
        }
        return Result.success(Unit)
    }

    override suspend fun getUserCoupons(userId: String): Result<List<UserCoupon>> =
        Result.success(userCoupons.filter { it.userId == userId })

    private fun generateQrToken(couponId: String, userId: String): String {
        val payload = "$couponId:$userId:${System.currentTimeMillis()}"
        return android.util.Base64.encodeToString(
            payload.toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
    }
}

class MockReservationRepository : ReservationRepository {
    private val reservations = MockData.reservations.toMutableList()

    override suspend fun createReservation(reservation: Reservation): Result<String> {
        delay(500)
        val id = UUID.randomUUID().toString()
        reservations.add(reservation.copy(id = id))
        return Result.success(id)
    }

    override suspend fun updateReservationStatus(reservationId: String, status: ReservationStatus): Result<Unit> {
        val idx = reservations.indexOfFirst { it.id == reservationId }
        if (idx >= 0) reservations[idx] = reservations[idx].copy(status = status)
        return Result.success(Unit)
    }

    override suspend fun getUserReservations(userId: String): Result<List<Reservation>> =
        Result.success(reservations.filter { it.userId == userId })

    override suspend fun getRestaurantReservations(restaurantId: String, date: String?): Result<List<Reservation>> =
        Result.success(
            reservations.filter { reservation ->
                reservation.restaurantId == restaurantId &&
                        (date == null || reservation.date == date)
            }
        )

    override fun observeReservations(restaurantId: String): Flow<List<Reservation>> =
        flow { emit(reservations.filter { it.restaurantId == restaurantId }) }
}

class MockFavoriteRepository : FavoriteRepository {
    private val favorites = mutableSetOf<String>()

    override suspend fun getUserFavorites(userId: String): Result<List<String>> =
        Result.success(favorites.toList())

    override suspend fun addFavorite(userId: String, restaurantId: String): Result<Unit> {
        favorites.add(restaurantId)
        return Result.success(Unit)
    }

    override suspend fun removeFavorite(userId: String, restaurantId: String): Result<Unit> {
        favorites.remove(restaurantId)
        return Result.success(Unit)
    }

    override suspend fun isFavorite(userId: String, restaurantId: String): Result<Boolean> =
        Result.success(restaurantId in favorites)

    override fun observeFavorites(userId: String): Flow<List<String>> =
        flow { emit(favorites.toList()) }
}

class MockInteractionRepository : InteractionRepository {
    override suspend fun logInteraction(
        userId: String,
        restaurantId: String,
        type: InteractionType,
        metadata: Map<String, String>
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getUserInteractions(userId: String): Result<List<Map<String, Any>>> =
        Result.success(emptyList())
}

// ─── Dependency Provider ──────────────────────────────────────────────────────

object RepositoryProvider {
    // Firebase real
    val auth: AuthRepository by lazy { FirebaseAuthRepository() }
    val restaurant: RestaurantRepository by lazy { FirebaseRestaurantRepository() }

    // Mock — substituir conforme integração Firestore avança
    val coupon: CouponRepository by lazy { FirebaseCouponRepository() }
    val reservation: ReservationRepository by lazy { FirebaseReservationRepository() }
    val favorite: FavoriteRepository by lazy { MockFavoriteRepository() }
    val interaction: InteractionRepository by lazy { MockInteractionRepository() }
}