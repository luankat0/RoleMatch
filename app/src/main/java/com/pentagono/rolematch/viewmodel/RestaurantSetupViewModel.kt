package com.pentagono.rolematch.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.pentagono.rolematch.model.RestaurantSetup

class RestaurantSetupViewModel : ViewModel(){
    private val _state = MutableStateFlow(RestaurantSetup())
    val state: StateFlow<RestaurantSetup> = _state

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step

    fun nextStep() {
        _step.value++
    }

    fun previousStep() {
        if (_step.value > 0) _step.value--
    }

    fun update(update: RestaurantSetup.() -> RestaurantSetup) {
        _state.value = _state.value.update()
    }
}