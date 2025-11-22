package com.example.controlacceso

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TarjetasActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var tarjetasAdapter: TarjetasAdapter
    private val listaTarjetas = mutableListOf<Tarjeta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tarjetas)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerTarjetas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        tarjetasAdapter = TarjetasAdapter(listaTarjetas)
        recyclerView.adapter = tarjetasAdapter

        cargarTarjetasDelUsuario()
    }

    private fun cargarTarjetasDelUsuario() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("TarjetasActivity", "Cargando tarjetas para userId: $userId")

        database.child("Tarjetas")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaTarjetas.clear()

                    Log.d("TarjetasActivity", "Tarjetas encontradas: ${snapshot.childrenCount}")

                    for (tarjetaSnapshot in snapshot.children) {
                        try {
                            val uid = tarjetaSnapshot.key
                            val nombre = tarjetaSnapshot.child("nombre").getValue(String::class.java)
                            val activa = tarjetaSnapshot.child("activa").getValue(Boolean::class.java) ?: false
                            val userIdValue = tarjetaSnapshot.child("userId").getValue(String::class.java)

                            if (uid != null && nombre != null) {
                                val tarjeta = Tarjeta(
                                    uid = uid,
                                    nombre = nombre,
                                    activa = activa,
                                    userId = userIdValue
                                )
                                listaTarjetas.add(tarjeta)
                                Log.d("TarjetasActivity", "Tarjeta cargada: $nombre - $uid")
                            }
                        } catch (e: Exception) {
                            Log.e("TarjetasActivity", "Error al parsear tarjeta", e)
                        }
                    }

                    tarjetasAdapter.notifyDataSetChanged()

                    if (listaTarjetas.isEmpty()) {
                        Toast.makeText(
                            this@TarjetasActivity,
                            "No tienes tarjetas registradas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TarjetasActivity", "Error al cargar tarjetas", error.toException())
                    Toast.makeText(
                        this@TarjetasActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}