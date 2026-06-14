package com.pentagono.rolematch

import com.pentagono.rolematch.domain.model.Address
import com.pentagono.rolematch.domain.model.Coupon
import com.pentagono.rolematch.domain.model.MatchPreferences
import com.pentagono.rolematch.domain.model.MatchResult
import com.pentagono.rolematch.domain.model.Reservation
import com.pentagono.rolematch.domain.model.ReservationStatus
import com.pentagono.rolematch.domain.model.Restaurant
import com.pentagono.rolematch.domain.model.RestaurantFeatures
import com.pentagono.rolematch.domain.model.RestaurantStatus
import com.pentagono.rolematch.domain.model.User
import com.pentagono.rolematch.domain.model.UserPreferences
import com.pentagono.rolematch.domain.model.UserRole
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

class UserModelTest {

    @Test
    fun `usuario criado com valores padrao deve ter role USER`() {
        val user = User()
        assertEquals(UserRole.USER, user.role)
    }

    @Test
    fun `usuario com nome valido deve ter nome correto`() {
        val user = User(name = "Miguel Ângelo")
        assertEquals("Miguel Ângelo", user.name)
    }

    @Test
    fun `usuario sem nome deve ter string vazia`() {
        val user = User()
        assertTrue(user.name.isEmpty())
    }

    @Test
    fun `usuario nao verificado por padrao`() {
        val user = User()
        assertFalse(user.isVerified)
    }

    @Test
    fun `usuario verificado deve retornar isVerified true`() {
        val user = User(isVerified = true)
        assertTrue(user.isVerified)
    }

    @Test
    fun `preferencias do usuario devem iniciar vazias`() {
        val prefs = UserPreferences()
        assertTrue(prefs.cuisineTypes.isEmpty())
        assertTrue(prefs.dietaryRestrictions.isEmpty())
        assertNull(prefs.priceRange)
    }

    @Test
    fun `distancia maxima padrao deve ser 5km`() {
        val prefs = UserPreferences()
        assertEquals(5f, prefs.maxDistance)
    }

    @Test
    fun `usuario com role ADMIN deve ser identificado corretamente`() {
        val admin = User(role = UserRole.ADMIN)
        assertEquals(UserRole.ADMIN, admin.role)
        assertNotEquals(UserRole.USER, admin.role)
    }

    @Test
    fun `usuario com role RESTAURANT deve ser identificado corretamente`() {
        val restaurantUser = User(role = UserRole.RESTAURANT)
        assertEquals(UserRole.RESTAURANT, restaurantUser.role)
    }

    @Test
    fun `email valido deve ser armazenado corretamente`() {
        val user = User(email = "miguel@rolematch.com")
        assertEquals("miguel@rolematch.com", user.email)
    }
}

class RestaurantModelTest {

    @Test
    fun `restaurante criado com valores padrao deve ter status PENDING_REVIEW`() {
        val restaurant = Restaurant()
        assertEquals(RestaurantStatus.PENDING_REVIEW, restaurant.status)
    }

    @Test
    fun `restaurante com nome valido deve armazenar nome corretamente`() {
        val restaurant = Restaurant(name = "Burguer RoleMatch")
        assertEquals("Burguer RoleMatch", restaurant.name)
    }

    @Test
    fun `restaurante ativo deve ter status ACTIVE`() {
        val restaurant = Restaurant(status = RestaurantStatus.ACTIVE)
        assertEquals(RestaurantStatus.ACTIVE, restaurant.status)
    }

    @Test
    fun `restaurante suspenso deve ter status SUSPENDED`() {
        val restaurant = Restaurant(status = RestaurantStatus.SUSPENDED)
        assertEquals(RestaurantStatus.SUSPENDED, restaurant.status)
    }

    @Test
    fun `faixa de preco padrao deve ser 2`() {
        val restaurant = Restaurant()
        assertEquals(2, restaurant.priceRange)
    }

    @Test
    fun `restaurante nao aceita reservas por padrao deve ser false`() {
        val restaurant = Restaurant(acceptsReservations = false)
        assertFalse(restaurant.acceptsReservations)
    }

    @Test
    fun `restaurante aceita reservas por padrao deve ser true`() {
        val restaurant = Restaurant()
        assertTrue(restaurant.acceptsReservations)
    }

    @Test
    fun `features do restaurante devem iniciar como false`() {
        val features = RestaurantFeatures()
        assertFalse(features.petFriendly)
        assertFalse(features.kidsArea)
        assertFalse(features.accessiblePcd)
        assertFalse(features.wifi)
        assertFalse(features.parking)
    }

    @Test
    fun `restaurante pet friendly deve ser identificado corretamente`() {
        val features = RestaurantFeatures(petFriendly = true)
        assertTrue(features.petFriendly)
    }

    @Test
    fun `endereco formatado deve conter rua e cidade`() {
        val address = Address(
            street = "Rua do Cemitério",
            number = "57",
            neighborhood = "Nossa Senhora do Ó",
            city = "Ipojuca"
        )
        val formatted = address.formatted()
        assertTrue(formatted.contains("Rua do Cemitério"))
        assertTrue(formatted.contains("Ipojuca"))
    }

    @Test
    fun `restaurante sem fotos deve ter lista vazia`() {
        val restaurant = Restaurant()
        assertTrue(restaurant.photos.isEmpty())
    }

    @Test
    fun `restaurante com fotos deve armazenar urls corretamente`() {
        val photos = listOf("https://example.com/foto1.jpg", "https://example.com/foto2.jpg")
        val restaurant = Restaurant(photos = photos)
        assertEquals(2, restaurant.photos.size)
        assertEquals("https://example.com/foto1.jpg", restaurant.photos.first())
    }

    @Test
    fun `rating padrao do restaurante deve ser zero`() {
        val restaurant = Restaurant()
        assertEquals(0f, restaurant.rating)
    }

    @Test
    fun `tipo de culinaria deve ser armazenado corretamente`() {
        val restaurant = Restaurant(cuisineTypes = listOf("Brasileira", "Italiana"))
        assertEquals(2, restaurant.cuisineTypes.size)
        assertTrue(restaurant.cuisineTypes.contains("Brasileira"))
    }
}

class CouponModelTest {

    @Test
    fun `cupom criado com valores padrao deve estar ativo`() {
        val coupon = Coupon()
        assertTrue(coupon.active)
    }

    @Test
    fun `cupom com titulo valido deve armazenar titulo corretamente`() {
        val coupon = Coupon(title = "20% OFF no jantar")
        assertEquals("20% OFF no jantar", coupon.title)
    }

    @Test
    fun `cupom de porcentagem deve ter benefitType correto`() {
        val coupon = Coupon(benefitType = "PERCENTAGE", discountValue = "20%")
        assertEquals("PERCENTAGE", coupon.benefitType)
        assertEquals("20%", coupon.discountValue)
    }

    @Test
    fun `cupom de valor fixo deve ter benefitType correto`() {
        val coupon = Coupon(benefitType = "FIXED", discountValue = "R$10")
        assertEquals("FIXED", coupon.benefitType)
    }

    @Test
    fun `cupom de item gratis deve ter benefitType correto`() {
        val coupon = Coupon(benefitType = "FREE_ITEM", discountValue = "Grátis")
        assertEquals("FREE_ITEM", coupon.benefitType)
    }

    @Test
    fun `numero maximo de usos padrao deve ser 100`() {
        val coupon = Coupon()
        assertEquals(100, coupon.maxUses)
    }

    @Test
    fun `maximo de usos por usuario padrao deve ser 1`() {
        val coupon = Coupon()
        assertEquals(1, coupon.maxUsesPerUser)
    }

    @Test
    fun `contagem de resgates deve iniciar em zero`() {
        val coupon = Coupon()
        assertEquals(0, coupon.redeemCount)
    }

    @Test
    fun `cupom inativo deve ser identificado corretamente`() {
        val coupon = Coupon(active = false)
        assertFalse(coupon.active)
    }

    @Test
    fun `cupom deve estar associado a um restaurante`() {
        val coupon = Coupon(restaurantId = "restaurante123")
        assertEquals("restaurante123", coupon.restaurantId)
        assertTrue(coupon.restaurantId.isNotEmpty())
    }

    @Test
    fun `status padrao do cupom deve ser ACTIVE`() {
        val coupon = Coupon()
        assertEquals("ACTIVE", coupon.status)
    }
}

class ReservationModelTest {

    @Test
    fun `reserva criada com valores padrao deve ter status PENDING`() {
        val reservation = Reservation()
        assertEquals(ReservationStatus.PENDING, reservation.status)
    }

    @Test
    fun `reserva deve armazenar nome do restaurante corretamente`() {
        val reservation = Reservation(restaurantName = "Burguer RoleMatch")
        assertEquals("Burguer RoleMatch", reservation.restaurantName)
    }

    @Test
    fun `numero de pessoas padrao deve ser 2`() {
        val reservation = Reservation()
        assertEquals(2, reservation.peopleCount)
    }

    @Test
    fun `reserva confirmada deve ter status CONFIRMED`() {
        val reservation = Reservation(status = ReservationStatus.CONFIRMED)
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
    }

    @Test
    fun `reserva cancelada deve ter status CANCELLED`() {
        val reservation = Reservation(status = ReservationStatus.CANCELLED)
        assertEquals(ReservationStatus.CANCELLED, reservation.status)
    }

    @Test
    fun `reserva deve armazenar data corretamente`() {
        val reservation = Reservation(date = "13/06/2026", time = "20:00")
        assertEquals("13/06/2026", reservation.date)
        assertEquals("20:00", reservation.time)
    }

    @Test
    fun `reserva deve estar associada a usuario e restaurante`() {
        val reservation = Reservation(
            userId = "user123",
            restaurantId = "restaurante456"
        )
        assertEquals("user123", reservation.userId)
        assertEquals("restaurante456", reservation.restaurantId)
    }
}

class MatchPreferencesTest {

    @Test
    fun `preferencias de match devem iniciar vazias`() {
        val prefs = MatchPreferences()
        assertTrue(prefs.cuisineTypes.isEmpty())
        assertTrue(prefs.establishmentTypes.isEmpty())
        assertTrue(prefs.dietaryRestrictions.isEmpty())
        assertNull(prefs.priceRange)
    }

    @Test
    fun `distancia maxima padrao deve ser 5km`() {
        val prefs = MatchPreferences()
        assertEquals(5f, prefs.maxDistanceKm)
    }

    @Test
    fun `pet friendly deve ser false por padrao`() {
        val prefs = MatchPreferences()
        assertFalse(prefs.petFriendly)
    }

    @Test
    fun `area kids deve ser false por padrao`() {
        val prefs = MatchPreferences()
        assertFalse(prefs.kidsArea)
    }

    @Test
    fun `acessivel pcd deve ser false por padrao`() {
        val prefs = MatchPreferences()
        assertFalse(prefs.accessiblePcd)
    }

    @Test
    fun `match com culinaria selecionada deve armazenar corretamente`() {
        val prefs = MatchPreferences(cuisineTypes = listOf("Brasileira", "Italiana"))
        assertEquals(2, prefs.cuisineTypes.size)
        assertTrue(prefs.cuisineTypes.contains("Brasileira"))
    }

    @Test
    fun `match result deve armazenar score corretamente`() {
        val restaurant = Restaurant(name = "Teste", status = RestaurantStatus.ACTIVE)
        val result = MatchResult(
            restaurant = restaurant,
            score = 85,
            matchReasons = listOf("Culinária Brasileira combinando", "⭐ Muito bem avaliado")
        )
        assertEquals(85, result.score)
        assertEquals(2, result.matchReasons.size)
    }

    @Test
    fun `match result sem razoes deve ter lista vazia`() {
        val restaurant = Restaurant(name = "Teste")
        val result = MatchResult(restaurant = restaurant, score = 0)
        assertTrue(result.matchReasons.isEmpty())
    }
}