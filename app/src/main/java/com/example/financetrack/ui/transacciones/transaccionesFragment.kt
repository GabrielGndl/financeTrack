package com.example.financetrack.ui.transacciones

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrack.R
import com.example.financetrack.data.model.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TransaccionesFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private lateinit var tabLayout: TabLayout
    private lateinit var chipGroupPeriodo: ChipGroup
    private lateinit var rvDesglose: RecyclerView
    private lateinit var viewModel: TransaccionesViewModel
    private lateinit var adapter: TransaccionesAdapter

    // NUEVAS VISTAS
    private lateinit var tvFechaActual: TextView
    private lateinit var btnFechaAnterior: ImageButton
    private lateinit var btnFechaSiguiente: ImageButton

    private var periodoSeleccionado = "Mes"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transacciones, container, false)
        pieChart = view.findViewById(R.id.pieChart)
        tabLayout = view.findViewById(R.id.tabLayout)
        chipGroupPeriodo = view.findViewById(R.id.chipGroupPeriodo)
        rvDesglose = view.findViewById(R.id.rvDesglose)

        // Enlazar navegador de fechas
        tvFechaActual = view.findViewById(R.id.tvFechaActual)
        btnFechaAnterior = view.findViewById(R.id.btnFechaAnterior)
        btnFechaSiguiente = view.findViewById(R.id.btnFechaSiguiente)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[TransaccionesViewModel::class.java]

        adapter = TransaccionesAdapter(emptyList()) { transaccion -> mostrarOpcionesTransaccion(transaccion) }
        rvDesglose.layoutManager = LinearLayoutManager(requireContext())
        rvDesglose.adapter = adapter

        // Pestañas
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { aplicarFiltros() }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Chips
        chipGroupPeriodo.setOnCheckedStateChangeListener { _, checkedIds ->
            periodoSeleccionado = when (checkedIds.firstOrNull()) {
                R.id.chipDia -> "Día"
                R.id.chipSemana -> "Semana"
                R.id.chipAño -> "Año"
                else -> "Mes"
            }
            aplicarFiltros()
        }

        // NUEVO: Flechas de navegación y Calendario
        btnFechaAnterior.setOnClickListener {
            viewModel.cambiarFecha(-1, periodoSeleccionado, tabLayout.selectedTabPosition == 0)
        }

        btnFechaSiguiente.setOnClickListener {
            viewModel.cambiarFecha(1, periodoSeleccionado, tabLayout.selectedTabPosition == 0)
        }

        tvFechaActual.setOnClickListener {
            mostrarSelectorFecha()
        }

        // Observadores
        viewModel.datosGrafico.observe(viewLifecycleOwner) { desglose -> actualizarGrafico(desglose) }
        viewModel.transaccionesFiltradas.observe(viewLifecycleOwner) { lista -> adapter.updateList(lista) }
        viewModel.textoFecha.observe(viewLifecycleOwner) { texto -> tvFechaActual.text = texto }

        view.findViewById<View>(R.id.fabAgregarTransaccion).setOnClickListener { mostrarDialogoNuevaTransaccion() }

        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val esGasto = tabLayout.selectedTabPosition == 0
        viewModel.filtrarDatos(esGasto, periodoSeleccionado)
    }

    // NUEVO: Abrir el calendario nativo
    private fun mostrarSelectorFecha() {
        val calendario = viewModel.fechaBase
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, anio, mes, dia ->
            val esGasto = tabLayout.selectedTabPosition == 0
            viewModel.setFechaExacta(anio, mes, dia, periodoSeleccionado, esGasto)
        }, year, month, day).show()
    }

    private fun mostrarOpcionesTransaccion(transaccion: Transaction) {
        val opciones = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones de Movimiento")
            .setItems(opciones) { _, posicion ->
                if (posicion == 0) mostrarDialogoEditar(transaccion)
                else viewModel.eliminarTransaccion(transaccion.id, tabLayout.selectedTabPosition == 0)
            }.show()
    }

    private fun mostrarDialogoEditar(t: Transaction) {
        val builder = AlertDialog.Builder(requireContext()).setTitle("Editar Registro")
        val layout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 40, 50, 10) }

        val inputMonto = EditText(requireContext()).apply {
            hint = "Monto"
            setText(t.amount.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(inputMonto)

        val categoriasBD = viewModel.obtenerCategorias(t.type)
        val nombres = categoriasBD.map { it.nombre }
        val spinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres)
            t.category.let { index -> val pos = nombres.indexOf(index); if (pos >= 0) setSelection(pos) }
        }
        layout.addView(spinner)

        val inputDesc = EditText(requireContext()).apply { hint = "Descripción"; setText(t.description) }
        layout.addView(inputDesc)
        builder.setView(layout)

        builder.setPositiveButton("Actualizar") { _, _ ->
            val monto = inputMonto.text.toString().toDoubleOrNull() ?: 0.0
            val cat = spinner.selectedItem?.toString() ?: "General"
            viewModel.editarTransaccion(t.id, monto, t.type, cat, t.date, inputDesc.text.toString())
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun mostrarDialogoNuevaTransaccion() {
        val esGasto = tabLayout.selectedTabPosition == 0
        val tipoTransaccion = if (esGasto) "GASTO" else "INGRESO"

        val builder = AlertDialog.Builder(requireContext()).setTitle("Registrar $tipoTransaccion")
        val layout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 40, 50, 10) }

        val inputMonto = EditText(requireContext()).apply {
            hint = "Monto"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(inputMonto)

        val categoriasBD = viewModel.obtenerCategorias(tipoTransaccion)
        val nombres = if (categoriasBD.isNotEmpty()) categoriasBD.map { it.nombre } else listOf("General")
        val spinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres)
        }
        layout.addView(spinner)

        val inputDesc = EditText(requireContext()).apply { hint = "Descripción" }
        layout.addView(inputDesc)
        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val montoText = inputMonto.text.toString()
            if (montoText.isNotEmpty()) {
                // Ahora usamos la fechaBase que el usuario esté viendo en pantalla
                val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(viewModel.fechaBase.time)
                viewModel.agregarTransaccion(montoText.toDouble(), tipoTransaccion, spinner.selectedItem.toString(), fechaRegistro, inputDesc.text.toString())
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun actualizarGrafico(lista: List<com.example.financetrack.data.model.CategoriaDesglose>) {
        val entries = lista.map { PieEntry(it.monto.toFloat(), it.nombre) }
        val dataSet = PieDataSet(entries, "").apply {
            val colores = ArrayList<Int>()
            for (c in ColorTemplate.MATERIAL_COLORS) colores.add(c)
            for (c in ColorTemplate.PASTEL_COLORS) colores.add(c)
            this.colors = colores
        }

        pieChart.data = PieData(dataSet)
        pieChart.centerText = "Total"
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.setHoleColor(Color.parseColor("#121212"))
        pieChart.description.isEnabled = false
        pieChart.legend.textColor = Color.WHITE
        pieChart.invalidate()
    }
}