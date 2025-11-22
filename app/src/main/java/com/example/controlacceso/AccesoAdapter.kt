package com.example.controlacceso

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.controlacceso.databinding.ItemAccesoBinding
import java.text.NumberFormat
import java.util.Locale

class AccesoAdapter(
    private val accesos: List<RegistroAcceso>,
    private val onDeleteClicked: (RegistroAcceso) -> Unit
) : RecyclerView.Adapter<AccesoAdapter.AccesoViewHolder>() {

    class AccesoViewHolder(val binding: ItemAccesoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccesoViewHolder {
        val binding = ItemAccesoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccesoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccesoViewHolder, position: Int) {
        val acceso = accesos[position]
        
        holder.binding.txtUID.text = "Tarjeta: ${acceso.uid ?: "N/A"}"
        holder.binding.txtUserEmail.text = acceso.userEmail ?: "(sin email)"
        holder.binding.txtFecha.text = "${acceso.fecha ?: ""} ${acceso.hora ?: ""}"
        holder.binding.txtEstado.text = "${acceso.estado ?: "N/A"}"

        val format = NumberFormat.getCurrencyInstance(Locale.US)
        holder.binding.txtCosto.text = format.format(acceso.costo ?: 0.0)

        val estadoColor = when {
            acceso.estado?.contains("Autorizado", ignoreCase = true) == true -> Color.parseColor("#4CAF50")
            acceso.estado?.contains("Denegado", ignoreCase = true) == true -> Color.parseColor("#F44336")
            else -> Color.parseColor("#757575") // Gris por defecto
        }
        holder.binding.txtEstado.setTextColor(estadoColor)

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClicked(acceso)
        }
    }

    override fun getItemCount(): Int = accesos.size
}
