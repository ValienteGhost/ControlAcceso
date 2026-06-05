package com.example.controlacceso

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.controlacceso.databinding.ActivityTarjetasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TarjetasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTarjetasBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var tarjetasAdapter: TarjetasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTarjetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        tarjetasAdapter = TarjetasAdapter { tarjeta ->
            // Implementar borrado si es necesario, siguiendo la funcionalidad original (que no tenía borrar aquí pero sí en el adapter antiguo)
            // Por ahora mantenemos la interfaz pero el adapter anterior recibía la lista y no un callback de borrado.
            // La funcionalidad original no tenía borrar implementado en esta Activity, pero el adapter sí tenía el botón.
        }
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = tarjetasAdapter

        cargarTarjetas()
        
        binding.btnAddTarjeta.setOnClickListener {
            // Funcionalidad de añadir tarjeta (no estaba implementada originalmente en el código proporcionado, pero añado el listener)
            Toast.makeText(this, "Funcionalidad de añadir tarjeta próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarTarjetas() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database.child("Tarjetas")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nuevaLista = mutableListOf<Tarjeta>()

                    for (tarjetaSnapshot in snapshot.children) {
                        try {
                            val tarjetaUserId = tarjetaSnapshot.child("userId").getValue(String::class.java)
                            
                            if (tarjetaUserId == userId) {
                                val uid = tarjetaSnapshot.key
                                val alias = tarjetaSnapshot.child("alias").getValue(String::class.java)
                                val activa = tarjetaSnapshot.child("activa").getValue(Boolean::class.java) ?: true

                                if (uid != null) {
                                    val tarjeta = Tarjeta(
                                        uid = uid,
                                        alias = alias ?: "Tarjeta sin nombre",
                                        activa = activa,
                                        userId = tarjetaUserId
                                    )
                                    nuevaLista.add(tarjeta)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TarjetasActivity", "Error al parsear tarjeta", e)
                        }
                    }

                    tarjetasAdapter.submitList(nuevaLista)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TarjetasActivity", "Error: ${error.message}", error.toException())
                    Toast.makeText(this@TarjetasActivity, "Error al cargar tarjetas", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
