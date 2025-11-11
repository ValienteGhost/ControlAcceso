package com.example.controlacceso

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.controlacceso.databinding.ActivityNuevoAccesoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class NuevoAccesoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoAccesoBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoAccesoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().getReference("Accesos")

        binding.btnGuardar.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: No se ha podido identificar al usuario.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val tipo = binding.txtEstado.text.toString().trim()

            if (tipo.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce un tipo de acceso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaCompleta = SimpleDateFormat("yyyy-MM-dd - HH:mm:ss", Locale.getDefault()).format(Date())
            val costo = 100.0

            val nuevoAcceso = RegistroAcceso(uid = userId, fecha = fechaCompleta, tipo = tipo, costo = costo)

            database.push().setValue(nuevoAcceso)
                .addOnSuccessListener {
                    Toast.makeText(this, "Acceso agregado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
