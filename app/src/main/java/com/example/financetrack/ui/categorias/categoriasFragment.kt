package com.example.financetrack.ui.categorias

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrack.R
import com.google.android.material.tabs.TabLayout

class CategoriasFragment : Fragment() {
    private lateinit var viewModel: CategoriasViewModel
    private lateinit var adapter: CategoriasAdapter
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_categorias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CategoriasViewModel::class.java]
        tabLayout = view.findViewById(R.id.tabLayoutCategorias)

        val rvCategorias = view.findViewById<RecyclerView>(R.id.rvCategorias)
        adapter = CategoriasAdapter(emptyList())
        rvCategorias.layoutManager = GridLayoutManager(requireContext(), 4)
        rvCategorias.adapter = adapter

        viewModel.categoriasVisibles.observe(viewLifecycleOwner) { lista -> adapter.updateList(lista) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { viewModel.cargarCategorias(tab?.position == 0) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        view.findViewById<View>(R.id.fabAgregarCategoria).setOnClickListener { abrirMenuCreacionCompleto() }
        viewModel.cargarCategorias(true)
    }

    private fun abrirMenuCreacionCompleto() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_crear_categoria)

        val btnVolver = dialog.findViewById<ImageButton>(R.id.btnVolver)
        val etNombre = dialog.findViewById<EditText>(R.id.etNombreCategoriaMenu)
        val gridSimbolos = dialog.findViewById<GridLayout>(R.id.gridSimbolos)
        val contenedorColores = dialog.findViewById<LinearLayout>(R.id.contenedorColores)
        val btnAnadir = dialog.findViewById<Button>(R.id.btnAnadirCategoria)

        btnVolver.setOnClickListener { dialog.dismiss() }

        val coloresHex = listOf("#00BCD4", "#2196F3", "#F44336", "#4CAF50", "#E91E63", "#9C27B0", "#FFC107", "#795548")
        val iconosRes = listOf(
            android.R.drawable.ic_menu_help, android.R.drawable.ic_menu_agenda, android.R.drawable.ic_menu_directions,
            android.R.drawable.ic_menu_manage, android.R.drawable.ic_menu_camera, android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_mapmode, android.R.drawable.ic_menu_send, android.R.drawable.ic_menu_call,
            android.R.drawable.ic_menu_zoom, android.R.drawable.ic_menu_compass, android.R.drawable.ic_menu_view
        )

        var colorSeleccionado = coloresHex[0]
        var iconoSeleccionado = iconosRes[0]

        // Listas para manejar el "Aura" visual
        val vistasColores = mutableListOf<View>()
        val vistasIconos = mutableListOf<ImageButton>()

        // Llenar Cuadrícula de Íconos
        iconosRes.forEach { resId ->
            val iconView = ImageButton(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply { width = 160; height = 160; setMargins(16, 16, 16, 16) }
                setImageResource(resId)
                setColorFilter(Color.WHITE)

                // Fondo por defecto (Transparente)
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.TRANSPARENT) }

                setOnClickListener {
                    iconoSeleccionado = resId
                    // Quitar aura de todos
                    vistasIconos.forEach { (it.background as GradientDrawable).setColor(Color.TRANSPARENT) }
                    // Poner aura al seleccionado (Gris claro translúcido)
                    (this.background as GradientDrawable).setColor(Color.parseColor("#44FFFFFF"))
                }
            }
            vistasIconos.add(iconView)
            gridSimbolos.addView(iconView)
        }

        // Seleccionar el primer ícono por defecto
        (vistasIconos[0].background as GradientDrawable).setColor(Color.parseColor("#44FFFFFF"))

        // Llenar Fila de Colores
        coloresHex.forEach { hex ->
            val colorView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(120, 120).apply { setMargins(16, 0, 16, 0) }

                // Fondo por defecto (Sin borde)
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor(hex)) }

                setOnClickListener {
                    colorSeleccionado = hex
                    // Quitar borde de todos
                    vistasColores.forEach { (it.background as GradientDrawable).setStroke(0, Color.TRANSPARENT) }
                    // Poner borde blanco grueso al seleccionado
                    (this.background as GradientDrawable).setStroke(8, Color.WHITE)
                }
            }
            vistasColores.add(colorView)
            contenedorColores.addView(colorView)
        }

        // Seleccionar el primer color por defecto
        (vistasColores[0].background as GradientDrawable).setStroke(8, Color.WHITE)

        // Guardar la categoría sin preguntar el tipo (lo deduce de la pestaña)
        btnAnadir.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isNotEmpty()) {
                // Lee en qué pestaña estamos actualmente (0 = Gastos, 1 = Ingresos)
                val tipo = if (tabLayout.selectedTabPosition == 0) "GASTO" else "INGRESO"
                viewModel.agregarCategoria(nombre, tipo, colorSeleccionado, iconoSeleccionado)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Por favor, ingresa un nombre", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}