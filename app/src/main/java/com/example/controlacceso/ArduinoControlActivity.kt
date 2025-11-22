package com.example.controlacceso

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.controlacceso.databinding.ActivityArduinoControlBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ArduinoControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArduinoControlBinding
    private lateinit var database: DatabaseReference
    private val prefs by lazy {
        getSharedPreferences("ArduinoPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArduinoControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Accesos")
        binding.etArduinoIP.setText(prefs.getString("arduino_ip", ""))

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnGuardarIP.setOnClickListener {
            val ip = binding.etArduinoIP.text.toString().trim()
            if (ip.isNotEmpty()) {
                prefs.edit().putString("arduino_ip", ip).apply()
                Toast.makeText(this, "IP guardada: $ip", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ingresa una IP válida", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón "Enviar a Arduino" ahora también registra en Firebase
        binding.btnEnviarArduino.setOnClickListener {
            val ip = prefs.getString("arduino_ip", "")
            val cardUid = binding.etUidManual.text.toString().trim().uppercase()

            if (ip.isNullOrEmpty()) {
                Toast.makeText(this, "Primero configura la IP del Arduino", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardUid.length != 8) {
                Toast.makeText(this, "UID debe tener 8 caracteres hexadecimales", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Primero, registrar en Firebase
            registrarAccesoEnFirebase(cardUid, "App Manual")
            // Luego, enviar el comando al Arduino
            enviarComandoArduino(ip, cardUid)
        }

        binding.btnTestConexion.setOnClickListener {
            val ip = prefs.getString("arduino_ip", "")
            if (!ip.isNullOrEmpty()) {
                testConexionArduino(ip)
            } else {
                Toast.makeText(this, "Configura la IP primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarAccesoEnFirebase(cardUid: String, tipo: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }
        
        val now = Date()
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)

        val acceso = RegistroAcceso(
            uid = cardUid,
            userId = user.uid,
            userEmail = user.email,
            fecha = fecha,
            hora = hora,
            tipo = tipo,
            estado = "Autorizado - Manual",
            costo = 0.0, // Sin costo para registros manuales
            timestamp = System.currentTimeMillis()
        )

        database.push().setValue(acceso.toMap())
            .addOnSuccessListener {
                Toast.makeText(this, "✓ Acceso registrado en Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "✗ Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enviarComandoArduino(ip: String, uid: String) {
        binding.btnEnviarArduino.isEnabled = false
        binding.btnEnviarArduino.text = "Enviando..."

        lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://$ip/login?uid=$uid")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Result.success("Comando enviado a Arduino")
                    } else {
                        Result.failure(Exception("Error HTTP: $responseCode"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            binding.btnEnviarArduino.isEnabled = true
            binding.btnEnviarArduino.text = "Enviar a Arduino"

            if (resultado.isSuccess) {
                Toast.makeText(this@ArduinoControlActivity, "✓ ${resultado.getOrNull()}", Toast.LENGTH_SHORT).show()
                updateEstadoConexion("Conectado", true)
            } else {
                Toast.makeText(this@ArduinoControlActivity, "✗ Error de envío: ${resultado.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                updateEstadoConexion("Error de conexión", false)
            }
        }
    }

    private fun testConexionArduino(ip: String) {
        binding.btnTestConexion.isEnabled = false
        binding.tvEstadoConexion.text = "Probando conexión..."

        lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://$ip")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    connection.connect()

                    val responseCode = connection.responseCode
                    Result.success(responseCode)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            binding.btnTestConexion.isEnabled = true

            if (resultado.isSuccess) {
                updateEstadoConexion("✓ Arduino conectado (HTTP ${resultado.getOrNull()})", true)
            } else {
                updateEstadoConexion("✗ Arduino desconectado", false)
            }
        }
    }

    private fun updateEstadoConexion(mensaje: String, exito: Boolean) {
        binding.tvEstadoConexion.text = mensaje
        val color = if (exito) {
            getColor(R.color.teal_700)
        } else {
            getColor(R.color.red_dark)
        }
        binding.tvEstadoConexion.setTextColor(color)
    }
}
