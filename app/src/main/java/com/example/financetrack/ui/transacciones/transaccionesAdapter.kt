package com.example.financetrack.ui.transacciones

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        val tvDesc: TextView = view.findViewById(R.id.tvDescripcion)
        val tvMonto: TextView = view.findViewById(R.id.tvMonto)
        val cvIcono: CardView = view.findViewById(R.id.cvIconoTransaccion) // NUEVO
        val ivIcono: ImageView = view.findViewById(R.id.ivIconoTransaccion) // NUEVO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_desglose, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.category
        holder.tvFecha.text = "${item.date} • ${item.hora}"
        // Pinta el monto de Verde si es Ingreso
        if (item.type == "INGRESO") {
            holder.tvMonto.text = "+ $ ${item.amount}"
            holder.tvMonto.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvMonto.text = "- $ ${item.amount}"
            holder.tvMonto.setTextColor(Color.WHITE)
        }

        if (!item.description.isNullOrEmpty()) {
            holder.tvDesc.text = " | ${item.description}"
            holder.tvDesc.visibility = View.VISIBLE
        } else {
            holder.tvDesc.visibility = View.GONE
        }

        // NUEVO: Aplicar el color y el ícono de la base de datos
        try {
            item.colorCategory?.let { holder.cvIcono.setCardBackgroundColor(Color.parseColor(it)) }
            item.iconCategory?.let { holder.ivIcono.setImageResource(it) }
        } catch (e: Exception) {}

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = lista.size

    fun updateList(nuevaLista: List<Transaction>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}