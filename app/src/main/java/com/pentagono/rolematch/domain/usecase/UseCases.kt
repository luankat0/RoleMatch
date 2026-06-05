package com.pentagono.rolematch.domain.usecase

import com.pentagono.rolematch.core.validators.*
import com.pentagono.rolematch.domain.model.*
import com.pentagono.rolematch.domain.repository.*

// ─── Auth Use Cases ───────────────────────────────────────────────────────────

class LoginUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (!EmailValidator.isValid(email)) return Result.failure(Exception("E-mail inválido"))
        if (password.length < 6) return Result.failure(Exception("Senha muito curta"))
        return repo.loginWithEmail(email, password)
    }
}

class RegisterUserUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(
        name: String, email: String, phone: String, cpf: String, password: String
    ): Result<User> {
        if (name.trim().length < 3) return Result.failure(Exception("Nome muito curto"))
        if (!EmailValidator.isValid(email)) return Result.failure(Exception("E-mail inválido"))
        if (!PhoneValidator.isValid(phone)) return Result.failure(Exception("Telefone inválido"))
        if (!CpfValidator.isValid(cpf)) return Result.failure(Exception("CPF inválido"))
        val pwd = PasswordValidator.validate(password)
        if (!pwd.isValid) return Result.failure(Exception(pwd.errors.joinToString(", ")))
        return repo.registerUser(email, password, name, phone, CpfValidator.strip(cpf))
    }
}

class RegisterRestaurantUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(
        name: String, email: String, phone: String, cnpj: String, password: String
    ): Result<User> {
        if (name.trim().length < 2) return Result.failure(Exception("Nome inválido"))
        if (!EmailValidator.isValid(email)) return Result.failure(Exception("E-mail inválido"))
        if (!PhoneValidator.isValid(phone)) return Result.failure(Exception("Telefone inválido"))
        if (!CnpjValidator.isValid(cnpj)) return Result.failure(Exception("CNPJ inválido"))
        val pwd = PasswordValidator.validate(password)
        if (!pwd.isValid) return Result.failure(Exception(pwd.errors.joinToString(", ")))
        return repo.registerRestaurant(email, password, name, phone, CnpjValidator.strip(cnpj))
    }
}

// ─── Coupon Use Cases ─────────────────────────────────────────────────────────

class IssueCouponUseCase(private val repo: CouponRepository) {
    suspend operator fun invoke(couponId: String, userId: String): Result<UserCoupon> {
        if (couponId.isBlank() || userId.isBlank())
            return Result.failure(Exception("Dados inválidos"))
        return repo.issueCouponToUser(couponId, userId)
    }
}

class ValidateCouponQrUseCase(
    private val couponRepo: CouponRepository,
    private val interactionRepo: InteractionRepository
) {
    suspend operator fun invoke(
        qrToken: String,
        restaurantId: String,
        operatorId: String
    ): Result<UserCoupon> {
        if (qrToken.isBlank()) return Result.failure(Exception("Token inválido"))
        val result = couponRepo.validateCouponQr(qrToken, restaurantId)
        if (result.isSuccess) {
            couponRepo.markCouponAsUsed(result.getOrThrow().id, operatorId)
        }
        return result
    }
}

// ─── Match Use Case ───────────────────────────────────────────────────────────

class GetMatchResultsUseCase(
    private val restaurantRepo: RestaurantRepository,
    private val interactionRepo: InteractionRepository
) {
    suspend operator fun invoke(
        preferences: MatchPreferences,
        userId: String
    ): Result<List<MatchResult>> {
        val restaurants = restaurantRepo
            .getNearbyRestaurants(0.0, 0.0, preferences.maxDistanceKm)
            .getOrElse { return Result.failure(it) }

        val scored = restaurants.map { restaurant ->
            var score = 0
            val reasons = mutableListOf<String>()

            // Cuisine match
            val cuisineMatches = restaurant.cuisineTypes.intersect(preferences.cuisineTypes.toSet()).size
            if (cuisineMatches > 0) {
                score += cuisineMatches * 30
                reasons += "Tipo de comida compatível"
            }

            // Establishment type match
            val typeMatches = restaurant.establishmentTypes.intersect(preferences.establishmentTypes.toSet()).size
            if (typeMatches > 0) {
                score += typeMatches * 20
                reasons += "Tipo de local compatível"
            }

            // Price range
            preferences.priceRange?.let { pref ->
                if (restaurant.priceRange == pref) {
                    score += 15
                    reasons += "Faixa de preço ideal"
                } else if (kotlin.math.abs(restaurant.priceRange - pref) == 1) {
                    score += 7
                }
            }

            // Distance
            if (restaurant.distanceKm <= preferences.maxDistanceKm) {
                val distScore = ((preferences.maxDistanceKm - restaurant.distanceKm) / preferences.maxDistanceKm * 15).toInt()
                score += distScore
                if (restaurant.distanceKm <= 1f) reasons += "Muito perto de você"
            }

            // Special features
            if (preferences.petFriendly && restaurant.features.petFriendly) { score += 10; reasons += "Pet Friendly" }
            if (preferences.kidsArea && restaurant.features.kidsArea) { score += 10; reasons += "Área Kids" }
            if (preferences.accessiblePcd && restaurant.features.accessiblePcd) { score += 10; reasons += "Acessível PcD" }

            // Rating bonus
            score += (restaurant.rating * 4).toInt()

            MatchResult(restaurant = restaurant, score = score, matchReasons = reasons)
        }

        return Result.success(
            scored
                .filter { it.score > 0 }
                .sortedByDescending { it.score }
        )
    }
}

// ─── Reservation Use Case ─────────────────────────────────────────────────────

class CreateReservationUseCase(private val repo: ReservationRepository) {
    suspend operator fun invoke(reservation: Reservation): Result<String> {
        if (reservation.restaurantId.isBlank()) return Result.failure(Exception("Restaurante inválido"))
        if (reservation.date.isBlank()) return Result.failure(Exception("Data obrigatória"))
        if (reservation.time.isBlank()) return Result.failure(Exception("Horário obrigatório"))
        if (reservation.peopleCount < 1) return Result.failure(Exception("Número de pessoas inválido"))
        if (reservation.customerName.isBlank()) return Result.failure(Exception("Nome obrigatório"))
        return repo.createReservation(reservation)
    }
}
