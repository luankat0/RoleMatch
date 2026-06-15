package com.pentagono.rolematch.di

import com.pentagono.rolematch.domain.repository.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun authRepository(): AuthRepository
    fun restaurantRepository(): RestaurantRepository
    fun couponRepository(): CouponRepository
    fun reservationRepository(): ReservationRepository
    fun favoriteRepository(): FavoriteRepository
    fun interactionRepository(): InteractionRepository
}