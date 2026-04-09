package com.pentagono.rolematch.model

data class RestaurantSetup(
    val address: String = "",
    val phone: String = "",
    val website: String = "",
    val instagram: String = "",
    val facebook: String = "",
    val description: String = "",

    val establishmentTypes: List<String> = emptyList(),
    val cuisineTypes: List<String> = emptyList(),
    val priceRange: Int? = null,

    val petFriendly: Boolean = false,
    val kidsArea: Boolean = false,

    val photos: List<String> = emptyList(),
    val menuLink: String = "",
    val mealVouchers: List<String> = emptyList()
)
