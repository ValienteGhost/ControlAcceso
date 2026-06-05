package com.example.controlacceso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.controlacceso.databinding.ItemAccesoBinding
import java.text.NumberFormat
import java.util.Locale

class AccesoAdapter(
    private val onDeleteClicked: (RegistroAcceso) -> Unit
) : ListAdapter<RegistroAcceso, AccesoAdapter.AccesoViewHolder>(AccesoDiffCallback()) {

    class AccesoViewHolder(val binding: ItemAccesoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccesoViewHolder {
        val binding = ItemAccesoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccesoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccesoViewHolder, position: Int) {
        val acceso = getItem(position)
        val context = holder.binding.root.context
        
        holder.binding.txtUID.text = context.getString(R.string.card_label, acceso.uid ?: "N/A")
        holder.binding.txtUserEmail.text = acceso.userEmail ?: context.getString(R.string.no_email)
        holder.binding.txtFecha.text = "${acceso.fecha ?: ""} ${acceso.hora ?: ""}"
        holder.binding.chipEstado.text = acceso.estado ?: "N/A"

        val format = NumberFormat.getCurrencyInstance(Locale.US)
        holder.binding.txtCosto.text = format.format(acceso.costo ?: 0.0)

        val isAuthorized = acceso.estado?.contains("Autorizado", ignoreCase = true) == true
        val isDenied = acceso.estado?.contains("Denegado", ignoreCase = true) == true

        val color = when {
            isAuthorized -> ContextCompat.getColor(context, R.color.success_green)
            isDenied -> ContextCompat.getColor(context, R.color.error)
            else -> ContextCompat.getColor(context, R.color.outline)
        }
        
        holder.binding.chipEstado.setTextColor(color)
        holder.binding.chipEstado.setChipStrokeColorResource(
            if (isAuthorized) R.color.success_green else if (isDenied) R.color.error else R.color.outline
        )

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClicked(acceso)
        }
    }

    class AccesoDiffCallback : DiffUtil.ItemCallback<RegistroAcceso>() {
        override fun areItemsTheSame(oldItem: RegistroAcceso, newItem: RegistroAcceso): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RegistroAcceso, newItem: RegistroAcceso): Boolean {
            return oldItem == newItem
        }
    }
}
