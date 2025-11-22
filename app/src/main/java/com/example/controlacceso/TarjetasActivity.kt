package com.example.controlacceso

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
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
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSinTarjetas: TextView
    private lateinit var tarjetasAdapter: TarjetasAdapter
    private val listaTarjetas = mutableListOf<Tarjeta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tarjetas)

        supportActionBar?.title = "Mis Tarjetas RFID"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerTarjetas)
        progressBar = findViewById(R.id.progressBar)
        tvSinTarjetas = findViewById(R.id.tvSinTarjetas)

        recyclerView.layoutManager = LinearLayoutManager(this)
        tarjetasAdapter = TarjetasAdapter(listaTarjetas)
        recyclerView.adapter = tarjetasAdapter

        cargarTarjetas()
    }

    private fun cargarTarjetas() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvSinTarjetas.visibility = View.GONE

        Log.d("TarjetasActivity", "Buscando tarjetas para userId: $userId")

        database.child("Tarjetas")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaTarjetas.clear()

                    Log.d("TarjetasActivity", "Total tarjetas en Firebase: ${snapshot.childrenCount}")

                    for (tarjetaSnapshot in snapshot.children) {
                        try {
                            val tarjetaUserId = tarjetaSnapshot.child("userId").getValue(String::class.java)
                            
                            if (tarjetaUserId == userId) {
                                val uid = tarjetaSnapshot.key
                                val nombre = tarjetaSnapshot.child("nombre").getValue(String::class.java)
                                val activa = tarjetaSnapshot.child("activa").getValue(Boolean::class.java) ?: true

                                if (uid != null) {
                                    val tarjeta = Tarjeta(
                                        uid = uid,
                                        nombre = nombre ?: "Tarjeta sin nombre",
                                        activa = activa,
                                        userId = tarjetaUserId
                                    )
                                    listaTarjetas.add(tarjeta)
                                    Log.d("TarjetasActivity", "Tarjeta encontrada: ${tarjeta.nombre} - $uid")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TarjetasActivity", "Error al parsear tarjeta", e)
                        }
                    }

                    progressBar.visibility = View.GONE

                    if (listaTarjetas.isEmpty()) {
                        tvSinTarjetas.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvSinTarjetas.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        tarjetasAdapter.notifyDataSetChanged()
                    }

                    Log.d("TarjetasActivity", "Total tarjetas del usuario: ${listaTarjetas.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Log.e("TarjetasActivity", "Error: ${error.message}", error.toException())
                    Toast.makeText(
                        this@TarjetasActivity,
                        "Error al cargar tarjetas: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}