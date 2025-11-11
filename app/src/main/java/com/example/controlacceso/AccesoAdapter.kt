package com.example.controlacceso

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
        holder.binding.txtUID.text = "UID: ${acceso.uid}"
        holder.binding.txtFecha.text = "Fecha: ${acceso.fecha}"
        holder.binding.txtTipo.text = "Tipo: ${acceso.tipo}"

        val format = NumberFormat.getCurrencyInstance(Locale.US)
        holder.binding.txtCosto.text = format.format(acceso.costo ?: 0.0)

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClicked(acceso)
        }
    }

    override fun getItemCount(): Int = accesos.size
}
