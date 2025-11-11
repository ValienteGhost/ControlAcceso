package com.example.controlacceso

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.controlacceso.databinding.ActivityPanelBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelBinding
    private lateinit var accessDatabase: DatabaseReference
    private lateinit var userDatabase: DatabaseReference
    private val listaAccesos = mutableListOf<RegistroAcceso>()
    private lateinit var adaptador: AccesoAdapter
    private lateinit var auth: FirebaseAuth
    private var currentBalance: Double = 0.0
    private val TICKET_COST = 100.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        accessDatabase = FirebaseDatabase.getInstance().getReference("Accesos")
        userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        adaptador = AccesoAdapter(listaAccesos) { acceso -> deleteAccess(acceso) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adaptador

        setupBalanceListener()
        setupAccessHistoryListener()

        binding.btnAddBalance.setOnClickListener { showBalanceDialog(true) }
        binding.btnSpendBalance.setOnClickListener { showBalanceDialog(false) }
    }

    private fun setupBalanceListener() {
        userDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.child("balance").getValue(Double::class.java) ?: 0.0
                currentBalance = balance
                val format = NumberFormat.getCurrencyInstance(Locale.US)
                binding.tvBalance.text = format.format(balance)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PanelActivity, "Error al cargar el saldo", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAccessHistoryListener() {
        accessDatabase.orderByChild("uid").equalTo(auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaAccesos.clear()
                for (item in snapshot.children) {
                    val acceso = item.getValue(RegistroAcceso::class.java)
                    if (acceso != null) {
                        acceso.id = item.key
                        listaAccesos.add(acceso)
                    }
                }
                listaAccesos.sortByDescending { it.fecha }
                adaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PanelActivity, "Error al cargar el historial", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteAccess(acceso: RegistroAcceso) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Borrado")
        builder.setMessage("¿Estás seguro de que quieres eliminar este registro de acceso?")
        builder.setPositiveButton("Eliminar") { dialog, _ ->
            acceso.id?.let {
                accessDatabase.child(it).removeValue()
                    .addOnSuccessListener { Toast.makeText(this@PanelActivity, "Registro eliminado", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this@PanelActivity, "Error al eliminar", Toast.LENGTH_SHORT).show() }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showBalanceDialog(isAdding: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isAdding) "Añadir Saldo" else "Gastar Saldo")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Introduce la cantidad"
        builder.setView(input)
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            val amountStr = input.text.toString()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDouble()
                updateBalance(amount, isAdding)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun updateBalance(amount: Double, isAdding: Boolean) {
        val newBalance = if (isAdding) currentBalance + amount else currentBalance - amount
        if (newBalance < 0) {
            Toast.makeText(this, "No tienes saldo suficiente", Toast.LENGTH_SHORT).show()
            return
        }
        userDatabase.child("balance").setValue(newBalance)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.panel_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                 Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_add_access -> {
                if (currentBalance >= TICKET_COST) {
                    updateBalance(TICKET_COST, isAdding = false)
                    val userId = auth.currentUser!!.uid
                    val fechaCompleta = SimpleDateFormat("yyyy-MM-dd - HH:mm:ss", Locale.getDefault()).format(Date())
                    val nuevoAcceso = RegistroAcceso(uid = userId, fecha = fechaCompleta, tipo = "Web", costo = TICKET_COST)
                    accessDatabase.push().setValue(nuevoAcceso.toMap())
                    Toast.makeText(this, "Pasaje comprado y descontado", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No tienes saldo suficiente para comprar un pasaje", Toast.LENGTH_LONG).show()
                }
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
