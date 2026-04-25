package com.pentagono.rolematch.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.pentagono.rolematch.model.RestaurantSetup

class RestaurantSetupViewModel : ViewModel() {

    private val _state = MutableStateFlow(RestaurantSetup())
    val state: StateFlow<RestaurantSetup> = _state

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step

    private val maxStep = 2 // 👈 agora temos 3 telas (0,1,2)

    fun nextStep() {
        if (_step.value < maxStep) {
            _step.value++
        }
    }

    fun previousStep() {
        if (_step.value > 0) {
            _step.value--
        }
    }

    fun update(block: RestaurantSetup.() -> RestaurantSetup) {
        _state.value = _state.value.block()
    }
}