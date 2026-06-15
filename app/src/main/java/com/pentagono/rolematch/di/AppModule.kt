package com.pentagono.rolematch.di

import com.pentagono.rolematch.data.repository.*
import com.pentagono.rolematch.domain.repository.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = FirebaseAuthRepository()

    @Provides
    @Singleton
    fun provideRestaurantRepository(): RestaurantRepository = FirebaseRestaurantRepository()

    @Provides
    @Singleton
    fun provideCouponRepository(): CouponRepository = FirebaseCouponRepository()

    @Provides
    @Singleton
    fun provideReservationRepository(): ReservationRepository = FirebaseReservationRepository()

    @Provides
    @Singleton
    fun provideFavoriteRepository(): FavoriteRepository = MockFavoriteRepository()

    @Provides
    @Singleton
    fun provideInteractionRepository(): InteractionRepository = MockInteractionRepository()
}