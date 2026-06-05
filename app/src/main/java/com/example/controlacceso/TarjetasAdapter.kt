package com.example.controlacceso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.controlacceso.databinding.ItemTarjetaBinding

class TarjetasAdapter(
    private val onDeleteClicked: (Tarjeta) -> Unit
) : ListAdapter<Tarjeta, TarjetasAdapter.TarjetaViewHolder>(TarjetaDiffCallback()) {

    class TarjetaViewHolder(val binding: ItemTarjetaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarjetaViewHolder {
        val binding = ItemTarjetaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TarjetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TarjetaViewHolder, position: Int) {
        val tarjeta = getItem(position)
        holder.binding.txtAlias.text = tarjeta.alias ?: "Sin nombre"
        holder.binding.txtUID.text = "UID: ${tarjeta.uid ?: "N/A"}"

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClicked(tarjeta)
        }
    }

    class TarjetaDiffCallback : DiffUtil.ItemCallback<Tarjeta>() {
        override fun areItemsTheSame(oldItem: Tarjeta, newItem: Tarjeta): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Tarjeta, newItem: Tarjeta): Boolean {
            return oldItem == newItem
        }
    }
}
