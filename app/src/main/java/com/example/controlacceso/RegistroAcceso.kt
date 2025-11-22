package com.example.controlacceso

import com.google.firebase.database.Exclude

data class RegistroAcceso(
    @get:Exclude
    var id: String? = null,
    val uid: String? = null,          // UID de la tarjeta
    val userId: String? = null,        // ID del usuario (IMPORTANTE)
    val userEmail: String? = null,     // NUEVO
    val fecha: String? = null,
    val hora: String? = null,
    val tipo: String? = null,
    val estado: String? = null,
    val costo: Double? = 100.0,
    val timestamp: Long? = 0
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "userId" to userId,
            "userEmail" to userEmail,
            "fecha" to fecha,
            "hora" to hora,
            "tipo" to tipo,
            "estado" to estado,
            "costo" to costo,
            "timestamp" to timestamp
        )
    }
}
