package com.example.financetrack.ui.categorias

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrack.R
import com.example.financetrack.data.model.Category

class CategoriasAdapter(private var lista: List<Category>) : RecyclerView.Adapter<CategoriasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCategoria)
        val cvContenedor: CardView = view.findViewById(R.id.cvIconoContenedor)
        val ivIcono: ImageView = view.findViewById(R.id.ivIconoCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre

        try {
            holder.cvContenedor.setCardBackgroundColor(Color.parseColor(item.color))
            holder.ivIcono.setImageResource(item.icono)
        } catch (e: Exception) {
            // Protección por si el color guardado está corrupto
        }
    }

    override fun getItemCount() = lista.size

    fun updateList(nuevaLista: List<Category>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}