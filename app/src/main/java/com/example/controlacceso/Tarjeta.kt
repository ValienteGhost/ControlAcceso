package com.example.controlacceso

data class Tarjeta(
    var uid: String? = null,
    val alias: String? = null,
    val activa: Boolean = true,
    val userId: String? = null
)