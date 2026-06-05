package com.pentagono.rolematch.domain.model

import java.util.Date

// ─── Enums ───────────────────────────────────────────────────────────────────

enum class UserRole { USER, RESTAURANT, ADMIN }

enum class RestaurantStatus { ACTIVE, PENDING_REVIEW, SUSPENDED, REJECTED }

enum class CouponStatus { AVAILABLE, USED, EXPIRED, BLOCKED }

enum class ReservationStatus { PENDING, CONFIRMED, CANCELLED, COMPLETED }

enum class ServiceMode { PRESENTIAL, DELIVERY, BOTH }

enum class InteractionType {
    LIKE, DISLIKE, FAVORITE, VIEW,
    COUPON_REDEEM, SEARCH, FILTER_USE,
    RESERVATION, REVIEW
}

// ─── User ────────────────────────────────────────────────────────────────────

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val cpf: String = "",
    val role: UserRole = UserRole.USER,
    val photoUrl: String = "",
    val isVerified: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class UserPreferences(
    val cuisineTypes: List<String> = emptyList(),
    val establishmentTypes: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val priceRange: Int? = null,
    val maxDistance: Float = 5f,
    val specialFeatures: List<String> = emptyList()
)

// ─── Restaurant ───────────────────────────────────────────────────────────────

data class Restaurant(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val legalName: String = "",
    val cnpj: String = "",
    val description: String = "",
    val category: String = "",
    val cuisineTypes: List<String> = emptyList(),
    val establishmentTypes: List<String> = emptyList(),
    val address: Address = Address(),
    val geoLat: Double = 0.0,
    val geoLng: Double = 0.0,
    val phone: String = "",
    val whatsapp: String = "",
    val website: String = "",
    val instagram: String = "",
    val facebook: String = "",
    val priceRange: Int = 2,
    val openingHours: Map<String, OpeningHour> = emptyMap(),
    val features: RestaurantFeatures = RestaurantFeatures(),
    val photos: List<String> = emptyList(),
    val menuLink: String = "",
    val menuPdfUrl: String = "",
    val serviceMode: ServiceMode = ServiceMode.PRESENTIAL,
    val acceptsReservations: Boolean = true,
    val mealVouchers: List<String> = emptyList(),
    val status: RestaurantStatus = RestaurantStatus.PENDING_REVIEW,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val distanceKm: Float = 0f,
    val isOpen: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class Address(
    val cep: String = "",
    val street: String = "",
    val number: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = "",
    val complement: String = ""
) {
    fun formatted() = "$street, $number – $neighborhood, $city"
}

data class OpeningHour(
    val open: String = "",
    val close: String = "",
    val closed: Boolean = false
)

data class RestaurantFeatures(
    val petFriendly: Boolean = false,
    val kidsArea: Boolean = false,
    val accessiblePcd: Boolean = false,
    val acceptsVr: Boolean = false,
    val wifi: Boolean = false,
    val parking: Boolean = false,
    val airConditioning: Boolean = false
)

// ─── Coupon ───────────────────────────────────────────────────────────────────

data class Coupon(
    val id: String = "",
    val restaurantId: String = "",
    val title: String = "",
    val description: String = "",
    val benefitType: String = "PERCENTAGE",    // PERCENTAGE | FIXED | FREE_ITEM
    val discountValue: String = "",            // "20%", "R$10", "Grátis"
    val rules: String = "",
    val validFrom: Date = Date(),
    val validUntil: Date = Date(),
    val maxUses: Int = 100,
    val maxUsesPerUser: Int = 1,
    val active: Boolean = true,
    val status: String = "ACTIVE",
    val redeemCount: Int = 0,
    val internalCode: String = "",
    val createdAt: Date = Date()
)

data class UserCoupon(
    val id: String = "",
    val couponId: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val couponTitle: String = "",
    val discountValue: String = "",
    val qrToken: String = "",
    val qrHash: String = "",
    val status: CouponStatus = CouponStatus.AVAILABLE,
    val issuedAt: Date = Date(),
    val expiresAt: Date = Date(),
    val usedAt: Date? = null,
    val validatedBy: String = ""
)

// ─── Reservation ─────────────────────────────────────────────────────────────

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val date: String = "",
    val time: String = "",
    val peopleCount: Int = 2,
    val occasionType: String = "",
    val customerName: String = "",
    val phone: String = "",
    val email: String = "",
    val notes: String = "",
    val status: ReservationStatus = ReservationStatus.PENDING,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

// ─── Review ───────────────────────────────────────────────────────────────────

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val restaurantId: String = "",
    val rating: Float = 5f,
    val comment: String = "",
    val photos: List<String> = emptyList(),
    val restaurantReply: String = "",
    val status: String = "PUBLISHED",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

// ─── Notification ─────────────────────────────────────────────────────────────

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val read: Boolean = false,
    val createdAt: Date = Date(),
    val metadata: Map<String, String> = emptyMap()
)

// ─── Match ────────────────────────────────────────────────────────────────────

data class MatchPreferences(
    val cuisineTypes: List<String> = emptyList(),
    val establishmentTypes: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val priceRange: Int? = null,
    val maxDistanceKm: Float = 5f,
    val petFriendly: Boolean = false,
    val kidsArea: Boolean = false,
    val accessiblePcd: Boolean = false
)

data class MatchResult(
    val restaurant: Restaurant,
    val score: Int,
    val matchReasons: List<String> = emptyList()
)
