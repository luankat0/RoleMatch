package com.pentagono.rolematch.domain.model

data class User(
    val id: String,
    val email: String,
    val isPremium: Boolean,
    var benefitsUsed: Int = 0
) {
    fun canUseBenefit(): Boolean {
        return if (isPremium) {
            true // Assinantes possuem benefícios ilimitados
        } else {
            benefitsUsed < 5 // Limite de 5 para não assinantes
        }
    }

    fun useBenefit() {
        if (canUseBenefit()) {
            benefitsUsed++
        } else {
            throw IllegalStateException("Limite de benefícios atingido para usuário não assinante.")
        }
    }
}