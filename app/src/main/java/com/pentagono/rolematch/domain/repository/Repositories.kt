package com.pentagono.rolematch.domain.repository

import com.pentagono.rolematch.domain.model.*
import kotlinx.coroutines.flow.Flow

// ─── Auth ─────────────────────────────────────────────────────────────────────

interface AuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun registerUser(email: String, password: String, name: String, phone: String, cpf: String): Result<User>
    suspend fun registerRestaurant(email: String, password: String, name: String, phone: String, cnpj: String): Result<User>
    suspend fun logout()
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendVerificationCode(email: String): Result<Unit>
    suspend fun verifyCode(email: String, code: String): Result<Unit>
    fun getCurrentUser(): User?
    fun observeAuthState(): Flow<User?>
}

// ─── User ─────────────────────────────────────────────────────────────────────

interface UserRepository {
    suspend fun getUserById(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun updatePreferences(userId: String, preferences: UserPreferences): Result<Unit>
    fun observeUser(userId: String): Flow<User?>
}

// ─── Restaurant ───────────────────────────────────────────────────────────────

interface RestaurantRepository {
    suspend fun getRestaurantById(id: String): Result<com.pentagono.rolematch.domain.model.Restaurant>
    suspend fun getRestaurantByOwnerId(ownerId: String): Result<com.pentagono.rolematch.domain.model.Restaurant>
    suspend fun createRestaurant(restaurant: com.pentagono.rolematch.domain.model.Restaurant): Result<String>
    suspend fun updateRestaurant(restaurant: com.pentagono.rolematch.domain.model.Restaurant): Result<Unit>
    suspend fun searchRestaurants(query: String): Result<List<com.pentagono.rolematch.domain.model.Restaurant>>
    suspend fun getNearbyRestaurants(lat: Double, lng: Double, radiusKm: Float): Result<List<com.pentagono.rolematch.domain.model.Restaurant>>
    suspend fun getFeaturedRestaurants(): Result<List<com.pentagono.rolematch.domain.model.Restaurant>>
    fun observeRestaurant(id: String): Flow<com.pentagono.rolematch.domain.model.Restaurant?>
}

// ─── Coupon ───────────────────────────────────────────────────────────────────

interface CouponRepository {
    suspend fun getCouponById(id: String): Result<Coupon>
    suspend fun getCouponsByRestaurant(restaurantId: String): Result<List<Coupon>>
    suspend fun getCouponsWithAvailability(): Result<List<Coupon>>
    suspend fun createCoupon(coupon: Coupon): Result<String>
    suspend fun updateCoupon(coupon: Coupon): Result<Unit>
    suspend fun deleteCoupon(couponId: String): Result<Unit>
    suspend fun issueCouponToUser(couponId: String, userId: String): Result<UserCoupon>
    suspend fun validateCouponQr(qrToken: String, restaurantId: String): Result<UserCoupon>
    suspend fun markCouponAsUsed(userCouponId: String, operatorId: String): Result<Unit>
    suspend fun getUserCoupons(userId: String): Result<List<UserCoupon>>
}

// ─── Reservation ─────────────────────────────────────────────────────────────

interface ReservationRepository {
    suspend fun createReservation(reservation: Reservation): Result<String>
    suspend fun updateReservationStatus(reservationId: String, status: ReservationStatus): Result<Unit>
    suspend fun getUserReservations(userId: String): Result<List<Reservation>>
    suspend fun getRestaurantReservations(restaurantId: String, date: String? = null): Result<List<Reservation>>
    fun observeReservations(restaurantId: String): Flow<List<Reservation>>
}

// ─── Review ───────────────────────────────────────────────────────────────────

interface ReviewRepository {
    suspend fun getReviewsByRestaurant(restaurantId: String): Result<List<Review>>
    suspend fun getUserReviews(userId: String): Result<List<Review>>
    suspend fun createReview(review: Review): Result<String>
    suspend fun replyToReview(reviewId: String, reply: String): Result<Unit>
}

// ─── Favorite ─────────────────────────────────────────────────────────────────

interface FavoriteRepository {
    suspend fun getUserFavorites(userId: String): Result<List<String>>
    suspend fun addFavorite(userId: String, restaurantId: String): Result<Unit>
    suspend fun removeFavorite(userId: String, restaurantId: String): Result<Unit>
    suspend fun isFavorite(userId: String, restaurantId: String): Result<Boolean>
    fun observeFavorites(userId: String): Flow<List<String>>
}

// ─── Interaction ──────────────────────────────────────────────────────────────

interface InteractionRepository {
    suspend fun logInteraction(
        userId: String,
        restaurantId: String,
        type: InteractionType,
        metadata: Map<String, String> = emptyMap()
    ): Result<Unit>

    suspend fun getUserInteractions(userId: String): Result<List<Map<String, Any>>>
}

// ─── Notification ─────────────────────────────────────────────────────────────

interface NotificationRepository {
    suspend fun getNotifications(userId: String): Result<List<AppNotification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(userId: String): Result<Unit>
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
}
