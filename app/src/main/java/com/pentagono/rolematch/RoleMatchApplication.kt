package com.pentagono.rolematch

import android.app.Application
import com.pentagono.rolematch.di.AppComponent
import com.pentagono.rolematch.di.DaggerAppComponent

class RoleMatchApplication : Application() {

    // Componente Dagger acessível em todo o app via (application as RoleMatchApplication).appComponent
    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}