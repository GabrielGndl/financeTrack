package com.example.financetrack.ui.transacciones

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrack.R
import com.example.financetrack.data.model.Transaction

class TransaccionesAdapter(
    private var lista: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransaccionesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCategoria)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaTransaccion)
        val tvDesc: TextView = view.findViewById(R.id.tvDescripcion) // NUEVO
        val tvMonto: TextView = view.findViewById(R.id.tvMonto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_desglose, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.category ?: "General"
        holder.tvFecha.text = item.date ?: ""
        holder.tvMonto.text = "$ ${item.amount}"

        // Mostrar la descripción solo si existe
        if (!item.description.isNullOrEmpty()) {
            holder.tvDesc.text = " | ${item.description}"
            holder.tvDesc.visibility = View.VISIBLE
        } else {
            holder.tvDesc.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = lista.size

    fun updateList(nuevaLista: List<Transaction>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}