package com.example.controlacceso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.controlacceso.databinding.ItemTarjetaBinding

class TarjetasAdapter(
    private val tarjetas: List<TarjetaRFID>,
    private val onDelete: (TarjetaRFID) -> Unit,
    private val onToggle: (TarjetaRFID) -> Unit
) : RecyclerView.Adapter<TarjetasAdapter.TarjetaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarjetaViewHolder {
        val binding = ItemTarjetaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TarjetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TarjetaViewHolder, position: Int) {
        holder.bind(tarjetas[position])
    }

    override fun getItemCount() = tarjetas.size

    inner class TarjetaViewHolder(private val binding: ItemTarjetaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tarjeta: TarjetaRFID) {
            binding.tvNombreTarjeta.text = tarjeta.nombre
            binding.tvUidTarjeta.text = "UID: ${tarjeta.uid}"
            binding.tvEstadoTarjeta.text = if (tarjeta.activa) "● Activa" else "○ Inactiva"
            binding.tvEstadoTarjeta.setTextColor(
                if (tarjeta.activa) 
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                else 
                    binding.root.context.getColor(android.R.color.darker_gray)
            )

            binding.switchActiva.isChecked = tarjeta.activa
            binding.switchActiva.setOnCheckedChangeListener { _, _ ->
                onToggle(tarjeta)
            }

            binding.btnEliminarTarjeta.setOnClickListener {
                onDelete(tarjeta)
            }
        }
    }
}