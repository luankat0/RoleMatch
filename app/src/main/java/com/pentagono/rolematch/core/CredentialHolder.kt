package com.pentagono.rolematch.core

object CredentialHolder {
    var email: String = ""
    var password: String = ""

    fun save(email: String, password: String) {
        this.email = email
        this.password = password
    }

    fun clear() {
        email = ""
        password = ""
    }
}