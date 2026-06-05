package com.pentagono.rolematch.core.common

import com.pentagono.rolematch.domain.model.*
import java.util.Calendar
import java.util.Date

object MockData {

    val restaurants = listOf(
        Restaurant(
            id = "1",
            name = "Trattoria Bella Vista",
            description = "Fundada em 1985 pela família Rossini, a Trattoria Bella Vista traz o verdadeiro sabor da Itália.",
            cuisineTypes = listOf("Italiana", "Mediterrânea"),
            establishmentTypes = listOf("Restaurante", "Bistrô"),
            priceRange = 3,
            rating = 4.5f,
            reviewCount = 127,
            distanceKm = 0.8f,
            isOpen = true,
            phone = "(11) 98765-4321",
            instagram = "@trattoriabellavista",
            website = "www.bellavista.com.br",
            address = Address("", "Rua das Flores", "123", "Centro", "São Paulo", "SP"),
            features = RestaurantFeatures(petFriendly = true, kidsArea = true),
            mealVouchers = listOf("Alelo", "VR", "Sodexo"),
            photos = listOf("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "2",
            name = "Sushi House Premium",
            description = "Sushi fresco e de qualidade com os melhores ingredientes.",
            cuisineTypes = listOf("Japonesa", "Asiática"),
            establishmentTypes = listOf("Restaurante"),
            priceRange = 4,
            rating = 4.8f,
            reviewCount = 89,
            distanceKm = 1.2f,
            isOpen = true,
            phone = "(11) 97654-3210",
            instagram = "@sushihousepremium",
            address = Address("", "Av. Paulista", "1500", "Bela Vista", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "3",
            name = "Le Bistrot Français",
            description = "Experiência gastronômica francesa no coração da cidade.",
            cuisineTypes = listOf("Francesa", "Contemporânea"),
            establishmentTypes = listOf("Bistrô", "Restaurante"),
            priceRange = 4,
            rating = 4.7f,
            reviewCount = 203,
            distanceKm = 2.0f,
            isOpen = true,
            address = Address("", "Rua Oscar Freire", "800", "Jardins", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "4",
            name = "Churrascaria Pampa",
            description = "A melhor carne gaúcha em São Paulo.",
            cuisineTypes = listOf("Brasileira", "Carnes"),
            establishmentTypes = listOf("Restaurante", "Churrascaria"),
            priceRange = 3,
            rating = 4.6f,
            reviewCount = 156,
            distanceKm = 2.5f,
            isOpen = false,
            address = Address("", "Av. Brigadeiro Faria Lima", "3000", "Itaim Bibi", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1544025162-d76694265947?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "5",
            name = "Burger & Co.",
            description = "Hambúrgueres artesanais com ingredientes premium.",
            cuisineTypes = listOf("Americana", "Lanches"),
            establishmentTypes = listOf("Hamburgueria", "Restaurante"),
            priceRange = 2,
            rating = 4.4f,
            reviewCount = 312,
            distanceKm = 0.9f,
            isOpen = true,
            address = Address("", "Rua Augusta", "1200", "Consolação", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "6",
            name = "Café do Centro",
            description = "O melhor café da manhã do centro da cidade.",
            cuisineTypes = listOf("Café", "Lanches"),
            establishmentTypes = listOf("Cafeteria", "Padaria"),
            priceRange = 1,
            rating = 4.2f,
            reviewCount = 78,
            distanceKm = 0.5f,
            isOpen = true,
            address = Address("", "Praça da Sé", "50", "Centro", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "7",
            name = "Thai Spice Garden",
            description = "Autêntica culinária tailandesa.",
            cuisineTypes = listOf("Tailandesa", "Asiática"),
            establishmentTypes = listOf("Restaurante"),
            priceRange = 3,
            rating = 4.6f,
            reviewCount = 94,
            distanceKm = 2.8f,
            isOpen = true,
            address = Address("", "Liberdade", "500", "Liberdade", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1562565652-a0d8f0c59eb4?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "8",
            name = "Pizzaria Napolitana",
            description = "Pizza no estilo napolitano com forno a lenha.",
            cuisineTypes = listOf("Italiana", "Pizza"),
            establishmentTypes = listOf("Pizzaria", "Restaurante"),
            priceRange = 2,
            rating = 4.4f,
            reviewCount = 201,
            distanceKm = 1.8f,
            isOpen = true,
            address = Address("", "Rua Hungria", "200", "Jardim Europa", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "9",
            name = "Bar do Alemão",
            description = "Cervejas artesanais e petiscos alemães.",
            cuisineTypes = listOf("Alemã", "Petiscos"),
            establishmentTypes = listOf("Bar", "Petiscaria"),
            priceRange = 2,
            rating = 4.3f,
            reviewCount = 145,
            distanceKm = 1.5f,
            isOpen = true,
            address = Address("", "Rua Bela Cintra", "300", "Consolação", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1559925393-8be0ec4767c8?w=400"),
            status = RestaurantStatus.ACTIVE
        ),
        Restaurant(
            id = "10",
            name = "La Cantina Mexicana",
            description = "Sabores autênticos do México.",
            cuisineTypes = listOf("Mexicana", "Tex-Mex"),
            establishmentTypes = listOf("Restaurante", "Bar"),
            priceRange = 2,
            rating = 4.5f,
            reviewCount = 167,
            distanceKm = 1.3f,
            isOpen = true,
            address = Address("", "Vila Madalena", "150", "Vila Madalena", "São Paulo", "SP"),
            photos = listOf("https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=400"),
            status = RestaurantStatus.ACTIVE
        )
    )

    val coupons = listOf(
        Coupon(
            id = "c1",
            restaurantId = "1",
            title = "20% OFF no jantar",
            description = "Válido de segunda a quinta-feira",
            benefitType = "PERCENTAGE",
            discountValue = "20%",
            rules = "Válido apenas para consumo no local. Não cumulativo.",
            validUntil = futureDate(30),
            internalCode = "JANTAR20",
            redeemCount = 45
        ),
        Coupon(
            id = "c2",
            restaurantId = "1",
            title = "Sobremesa grátis",
            description = "Na compra de prato principal",
            benefitType = "FREE_ITEM",
            discountValue = "Grátis",
            rules = "Válido apenas para sobremesas do cardápio padrão.",
            validUntil = futureDate(7),
            internalCode = "DOCEGRATIS",
            redeemCount = 32
        ),
        Coupon(
            id = "c3",
            restaurantId = "2",
            title = "Rodízio Premium",
            description = "Desconto especial no rodízio",
            benefitType = "PERCENTAGE",
            discountValue = "15%",
            validUntil = futureDate(14),
            internalCode = "RODIZIO15",
            redeemCount = 23
        ),
        Coupon(
            id = "c4",
            restaurantId = "6",
            title = "Café + Bolo",
            description = "Combo café e fatia de bolo",
            benefitType = "FIXED",
            discountValue = "R\$5",
            validUntil = futureDate(20),
            internalCode = "CAFEBOLO",
            redeemCount = 18
        ),
        Coupon(
            id = "c5",
            restaurantId = "8",
            title = "2ª Pizza 50% OFF",
            description = "Na compra da primeira pizza",
            benefitType = "PERCENTAGE",
            discountValue = "50%",
            validUntil = futureDate(10),
            internalCode = "PIZZA50",
            redeemCount = 67
        )
    )

    val reviews = listOf(
        Review(
            id = "r1",
            userId = "u1",
            userName = "Maria Silva",
            userPhotoUrl = "",
            restaurantId = "1",
            rating = 5f,
            comment = "Melhor comida italiana da região! O ambiente é acolhedor e o atendimento impecável. Recomendo muito!",
            restaurantReply = "Obrigada, Maria! Foi um prazer recebê-la. Volte sempre! 🍝",
            createdAt = pastDate(25)
        ),
        Review(
            id = "r2",
            userId = "u2",
            userName = "João Santos",
            userPhotoUrl = "",
            restaurantId = "1",
            rating = 4f,
            comment = "Comida excelente, preço justo. A área kids foi perfeita para levar as crianças.",
            createdAt = pastDate(28)
        )
    )

    val reservations = listOf(
        Reservation(
            id = "res1",
            restaurantId = "1",
            restaurantName = "Trattoria Bella Vista",
            date = "14/05/2026",
            time = "19:30",
            peopleCount = 4,
            customerName = "Maria Silva",
            status = ReservationStatus.CONFIRMED
        ),
        Reservation(
            id = "res2",
            restaurantId = "2",
            restaurantName = "Sushi House Premium",
            date = "19/05/2026",
            time = "20:00",
            peopleCount = 2,
            customerName = "Maria Silva",
            status = ReservationStatus.CONFIRMED
        ),
        Reservation(
            id = "res3",
            restaurantId = "6",
            restaurantName = "Café do Centro",
            date = "09/05/2026",
            time = "09:00",
            peopleCount = 3,
            customerName = "Maria Silva",
            status = ReservationStatus.COMPLETED
        )
    )

    val currentUser = User(
        id = "u1",
        name = "Maria Silva",
        email = "maria.silva@email.com",
        phone = "(11) 98765-4321",
        cpf = "123.456.789-00",
        role = UserRole.USER,
        isVerified = true,
        preferences = UserPreferences(
            dietaryRestrictions = listOf("Vegetariano", "Sem Glúten")
        )
    )

    val currentRestaurant = Restaurant(
        id = "rest1",
        ownerId = "owner1",
        name = "Trattoria Bella Vista",
        status = RestaurantStatus.ACTIVE,
        rating = 4.5f,
        reviewCount = 2
    )

    private fun futureDate(days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.time
    }

    private fun pastDate(days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.time
    }

    fun getCouponsForRestaurant(restaurantId: String) =
        coupons.filter { it.restaurantId == restaurantId }

    fun getRestaurantById(id: String) = restaurants.find { it.id == id }

    val popularSearches = listOf("Italiana", "Japonesa", "Pizza", "Hamburger", "Cafeteria", "Bar")

    val cuisineCategories = listOf(
        "Italiana", "Japonesa", "Brasileira", "Chinesa", "Mexicana", "Francesa",
        "Árabe", "Americana", "Tailandesa", "Indiana", "Coreana",
        "Mediterrânea", "Peruana", "Espanhola", "Portuguesa", "Grega"
    )

    val establishmentTypes = listOf(
        "Restaurante", "Cafeteria", "Bar", "Pizzaria", "Bistrô", "Churrascaria",
        "Lanchonete", "Padaria", "Hamburgueria", "Sorveteria", "Confeitaria", "Petiscaria"
    )

    val dietaryRestrictions = listOf(
        "Vegetariano", "Vegano", "Sem Glúten", "Sem Frutos do Mar", "Sem Amendoim"
    )
}