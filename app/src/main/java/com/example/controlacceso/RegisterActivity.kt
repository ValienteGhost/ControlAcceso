package com.example.controlacceso

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.controlacceso.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.txtEmail.text.toString().trim()
            val password = binding.txtPassword.text.toString().trim()
            val cardUid = binding.etCardUid.text.toString().trim().uppercase()

            if (email.isEmpty() || password.isEmpty() || cardUid.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cardUid.length != 8) {
                Toast.makeText(this, "El UID de la tarjeta debe tener 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            database.reference.child("Tarjetas").child(cardUid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Error: Esta tarjeta ya está registrada por otro usuario", Toast.LENGTH_LONG).show()
                    } else {
                        createUserAccount(email, password, cardUid)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al verificar la tarjeta: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun createUserAccount(email: String, password: String, cardUid: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user!!.uid
                    
                    val userProfile = mapOf("email" to email, "balance" to 0)
                    val tarjeta = Tarjeta(uid = cardUid, userId = userId, nombre = "Tarjeta Principal", activa = true)

                    val childUpdates = hashMapOf<
                        String, Any>(
                        "/Users/$userId" to userProfile,
                        "/Tarjetas/$cardUid" to tarjeta
                    )

                    database.reference.updateChildren(childUpdates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registro exitoso. Ahora puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { 
                            Toast.makeText(this, "Error al guardar datos: ${it.message}", Toast.LENGTH_LONG).show() 
                        }

                } else {
                    Toast.makeText(baseContext, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}