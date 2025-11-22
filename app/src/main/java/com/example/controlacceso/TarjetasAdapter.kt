package com.example.controlacceso

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TarjetasAdapter(private val tarjetas: List<Tarjeta>) :
    RecyclerView.Adapter<TarjetasAdapter.TarjetaViewHolder>() {

    class TarjetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreTarjeta)
        val tvUid: TextView = view.findViewById(R.id.tvUidTarjeta)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoTarjeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarjetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarjeta, parent, false)
        return TarjetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarjetaViewHolder, position: Int) {
        val tarjeta = tarjetas[position]

        holder.tvNombre.text = tarjeta.nombre
        holder.tvUid.text = "UID: ${tarjeta.uid}"
        
        if (tarjeta.activa) {
            holder.tvEstado.text = "✓ Activa"
            holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvEstado.text = "✗ Inactiva"
            holder.tvEstado.setTextColor(Color.parseColor("#F44336"))
        }
    }

    override fun getItemCount() = tarjetas.size
}
