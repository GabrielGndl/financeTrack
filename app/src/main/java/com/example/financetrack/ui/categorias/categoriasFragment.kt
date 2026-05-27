package com.example.financetrack.ui.categorias

import android.app.AlertDialog
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
import com.example.financetrack.data.model.Category
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

        // Inicializamos el adaptador con la función que reacciona a los clics
        adapter = CategoriasAdapter(emptyList()) { categoria ->
            mostrarOpcionesCategoria(categoria)
        }

        rvCategorias.layoutManager = GridLayoutManager(requireContext(), 4)
        rvCategorias.adapter = adapter

        viewModel.categoriasVisibles.observe(viewLifecycleOwner) { lista -> adapter.updateList(lista) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { viewModel.cargarCategorias(tab?.position == 0) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Botón FAB crea una categoría nueva (sin enviar datos previos)
        view.findViewById<View>(R.id.fabAgregarCategoria).setOnClickListener { abrirMenuCategoria(null) }
        viewModel.cargarCategorias(true)
    }

    // Menú de opciones rápidas al hacer clic en una categoría
    private fun mostrarOpcionesCategoria(categoria: Category) {
        val opciones = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(requireContext())
            .setTitle(categoria.nombre)
            .setItems(opciones) { _, posicion ->
                if (posicion == 0) abrirMenuCategoria(categoria) // Editar
                else viewModel.eliminarCategoria(categoria.id, tabLayout.selectedTabPosition == 0) // Borrar
            }.show()
    }

    // El menú de pantalla completa sirve tanto para crear como para editar
// El menú inferior emergente (BottomSheet) sirve tanto para crear como para editar
    private fun abrirMenuCategoria(categoriaAEditar: Category?) {
        // 1. Instanciamos el BottomSheetDialog en lugar del Dialog a pantalla completa
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_crear_categoria, null)
        bottomSheetDialog.setContentView(view)

        // 2. Enlazamos las vistas
        val tvTituloMenu = view.findViewById<TextView>(R.id.tvTituloMenu)
        val etNombre = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNombreCategoriaMenu)
        val gridSimbolos = view.findViewById<GridLayout>(R.id.gridSimbolos)
        val contenedorColores = view.findViewById<LinearLayout>(R.id.contenedorColores)
        val btnAnadir = view.findViewById<Button>(R.id.btnAnadirCategoria)

        val coloresHex = listOf("#00BCD4", "#2196F3", "#F44336", "#4CAF50", "#E91E63", "#9C27B0", "#FBC02D", "#795548")

        // Tus íconos nativos
        val iconosRes = listOf(
            R.drawable.ic_casa, R.drawable.ic_rayo, R.drawable.ic_agua, R.drawable.ic_wifi,
            R.drawable.ic_carrito, R.drawable.ic_restaurant, R.drawable.ic_perchero,
            R.drawable.ic_auto, R.drawable.ic_gasolina, R.drawable.ic_bus, R.drawable.ic_tren, R.drawable.ic_avion,
            R.drawable.ic_banco, R.drawable.ic_dinero, R.drawable.ic_ahorro,
            R.drawable.ic_medicina, R.drawable.ic_corazon, R.drawable.ic_pesas,
            R.drawable.ic_pelicula, R.drawable.ic_juegos, R.drawable.ic_colegio, R.drawable.ic_maleta
        )

        var colorSeleccionado = categoriaAEditar?.color ?: coloresHex[0]
        var iconoSeleccionado = categoriaAEditar?.icono ?: iconosRes[0]

        // Pre-rellenar textos si es edición
        if (categoriaAEditar != null) {
            tvTituloMenu.text = "Editar Categoría"
            etNombre.setText(categoriaAEditar.nombre)
            btnAnadir.text = "Actualizar"
        }

        val vistasColores = mutableListOf<View>()
        val vistasIconos = mutableListOf<ImageButton>()

        // Llenar Cuadrícula de Íconos
        iconosRes.forEach { resId ->
            val iconView = ImageButton(requireContext()).apply {
                // Reducimos un poco el tamaño para que quepan 5 en fila
                layoutParams = GridLayout.LayoutParams().apply { width = 140; height = 140; setMargins(12, 12, 12, 12) }
                setImageResource(resId)
                setColorFilter(Color.WHITE)

                val colorFondo = if (resId == iconoSeleccionado) Color.parseColor("#44FFFFFF") else Color.TRANSPARENT
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(colorFondo) }

                setOnClickListener {
                    iconoSeleccionado = resId
                    vistasIconos.forEach { (it.background as GradientDrawable).setColor(Color.TRANSPARENT) }
                    (this.background as GradientDrawable).setColor(Color.parseColor("#44FFFFFF"))
                }
            }
            vistasIconos.add(iconView)
            gridSimbolos.addView(iconView)
        }

        // Llenar Fila de Colores
        coloresHex.forEach { hex ->
            val colorView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(110, 110).apply { setMargins(12, 0, 12, 0) }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    if (hex == colorSeleccionado) setStroke(8, Color.WHITE)
                }

                setOnClickListener {
                    colorSeleccionado = hex
                    vistasColores.forEach { (it.background as GradientDrawable).setStroke(0, Color.TRANSPARENT) }
                    (this.background as GradientDrawable).setStroke(8, Color.WHITE)
                }
            }
            vistasColores.add(colorView)
            contenedorColores.addView(colorView)
        }

        btnAnadir.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isNotEmpty()) {
                val tipo = if (tabLayout.selectedTabPosition == 0) "GASTO" else "INGRESO"

                if (categoriaAEditar == null) {
                    viewModel.agregarCategoria(nombre, tipo, colorSeleccionado, iconoSeleccionado)
                } else {
                    viewModel.editarCategoria(categoriaAEditar.id, nombre, tipo, colorSeleccionado, iconoSeleccionado)
                }
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Por favor, ingresa un nombre", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }
}