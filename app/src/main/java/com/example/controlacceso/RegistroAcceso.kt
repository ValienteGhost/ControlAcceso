package com.example.controlacceso

import com.google.firebase.database.Exclude

data class RegistroAcceso(
    var id: String? = null,
    val uid: String? = null,
    val fecha: String? = null,
    val tipo: String? = null,
    val costo: Double? = 100.0
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "fecha" to fecha,
            "tipo" to tipo,
            "costo" to costo
        )
    }
}
