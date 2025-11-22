package com.example.controlacceso

data class Tarjeta(
    var uid: String? = null, // Cambiado a 'var' para poder ser modificado
    val nombre: String? = null,
    val activa: Boolean = true,
    val userId: String? = null
)