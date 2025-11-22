package com.example.controlacceso

data class Tarjeta(
    val uid: String? = null,
    val nombre: String? = null,
    val activa: Boolean = true,
    val userId: String? = null
)