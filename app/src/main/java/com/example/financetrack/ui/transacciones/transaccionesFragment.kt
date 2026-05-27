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
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.financetrack.data.model.CategoriaDesglose
import com.google.android.material.bottomsheet.BottomSheetDialog

class TransaccionesFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private lateinit var tabLayout: TabLayout
    private lateinit var chipGroupPeriodo: ChipGroup
    private lateinit var rvDesglose: RecyclerView
    private lateinit var viewModel: TransaccionesViewModel
    private lateinit var adapter: TransaccionesAdapter

    // VISTAS DEL NAVEGADOR
    private lateinit var tvFechaActual: TextView
    private lateinit var btnFechaAnterior: ImageButton
    private lateinit var btnFechaSiguiente: ImageButton

    // Variable para guardar el total y restaurarlo
    private var totalActual: Double = 0.0

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

        setupPieChart()

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

        // Flechas de navegación y Calendario
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

        // Observador modificado para el Estado Vacío
        viewModel.transaccionesFiltradas.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)

            val layoutEmptyState = view.findViewById<LinearLayout>(R.id.layoutEmptyState)

            if (lista.isEmpty()) {
                rvDesglose.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            } else {
                rvDesglose.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
            }
        }

        viewModel.textoFecha.observe(viewLifecycleOwner) { texto -> tvFechaActual.text = texto }

        view.findViewById<View>(R.id.fabAgregarTransaccion).setOnClickListener { mostrarDialogoNuevaTransaccion() }

        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val esGasto = tabLayout.selectedTabPosition == 0
        viewModel.filtrarDatos(esGasto, periodoSeleccionado)
    }

    // Abrir el calendario nativo
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
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opciones, null)
        bottomSheetDialog.setContentView(view)

        val btnEditar = view.findViewById<LinearLayout>(R.id.btnEditar)
        val btnEliminar = view.findViewById<LinearLayout>(R.id.btnEliminar)

        btnEditar.setOnClickListener {
            bottomSheetDialog.dismiss()
            mostrarDialogoEditar(transaccion)
        }

        btnEliminar.setOnClickListener {
            bottomSheetDialog.dismiss()
            val esGasto = tabLayout.selectedTabPosition == 0
            viewModel.eliminarTransaccion(transaccion.id, esGasto)
        }

        bottomSheetDialog.show()
    }

    private fun mostrarDialogoEditar(t: Transaction) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_nueva_transaccion, null)
        bottomSheetDialog.setContentView(view)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialogo)
        val inputMonto = view.findViewById<EditText>(R.id.etMonto)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategorias)
        val inputDesc = view.findViewById<EditText>(R.id.etDescripcion)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)

        tvTitulo.text = "Editar Registro"
        inputMonto.setText(t.amount.toString())
        inputDesc.setText(t.description ?: "")
        btnGuardar.text = "Actualizar"

        if (t.type == "INGRESO") {
            val colorVerde = Color.parseColor("#43A047")
            btnGuardar.backgroundTintList = android.content.res.ColorStateList.valueOf(colorVerde)
            btnGuardar.setTextColor(Color.WHITE)
            inputMonto.backgroundTintList = android.content.res.ColorStateList.valueOf(colorVerde)
            inputDesc.backgroundTintList = android.content.res.ColorStateList.valueOf(colorVerde)
        } else {
            val colorDorado = Color.parseColor("#FBC02D")
            btnGuardar.backgroundTintList = android.content.res.ColorStateList.valueOf(colorDorado)
            btnGuardar.setTextColor(Color.parseColor("#121212"))
            inputMonto.backgroundTintList = android.content.res.ColorStateList.valueOf(colorDorado)
            inputDesc.backgroundTintList = android.content.res.ColorStateList.valueOf(colorDorado)
        }

        val categoriasBD = viewModel.obtenerCategorias(t.type)
        val nombres = if (categoriasBD.isNotEmpty()) categoriasBD.map { it.nombre } else listOf("General")

        val adapterSpinner = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, nombres) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                v.setBackgroundColor(Color.parseColor("#1E1E1E"))
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
        }
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        val indexCategoria = nombres.indexOf(t.category)
        if (indexCategoria >= 0) {
            spinner.setSelection(indexCategoria)
        }

        btnCancelar.setOnClickListener { bottomSheetDialog.dismiss() }

        btnGuardar.setOnClickListener {
            val montoText = inputMonto.text.toString()
            if (montoText.isNotEmpty()) {
                val montoNuevo = montoText.toDoubleOrNull() ?: 0.0
                val catNueva = spinner.selectedItem?.toString() ?: "General"

                viewModel.editarTransaccion(
                    t.id,
                    montoNuevo,
                    t.type,
                    catNueva,
                    t.date,
                    inputDesc.text.toString()
                )
                bottomSheetDialog.dismiss()
            } else {
                inputMonto.error = "Por favor ingresa un monto"
            }
        }

        bottomSheetDialog.show()
    }

    private fun mostrarDialogoNuevaTransaccion() {
        val esGasto = tabLayout.selectedTabPosition == 0
        val tipoTransaccion = if (esGasto) "GASTO" else "INGRESO"

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_nueva_transaccion, null)
        bottomSheetDialog.setContentView(view)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialogo)
        val inputMonto = view.findViewById<EditText>(R.id.etMonto)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategorias)
        val inputDesc = view.findViewById<EditText>(R.id.etDescripcion)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)

        tvTitulo.text = "Registrar $tipoTransaccion"

        if (!esGasto) {
            val colorVerde = Color.parseColor("#43A047")
            btnGuardar.backgroundTintList = android.content.res.ColorStateList.valueOf(colorVerde)
            btnGuardar.setTextColor(Color.WHITE)
            inputMonto.backgroundTintList = android.content.res.ColorStateList.valueOf(colorVerde)
            inputDesc.backgroundTintList = android.content.res.ColorStateList.valueOf(colorVerde)
        } else {
            val colorDorado = Color.parseColor("#FBC02D")
            btnGuardar.backgroundTintList = android.content.res.ColorStateList.valueOf(colorDorado)
            btnGuardar.setTextColor(Color.parseColor("#121212"))
            inputMonto.backgroundTintList = android.content.res.ColorStateList.valueOf(colorDorado)
            inputDesc.backgroundTintList = android.content.res.ColorStateList.valueOf(colorDorado)
        }

        val categoriasBD = viewModel.obtenerCategorias(tipoTransaccion)
        val nombres = if (categoriasBD.isNotEmpty()) categoriasBD.map { it.nombre } else listOf("General")

        val adapterSpinner = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, nombres) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                v.setBackgroundColor(Color.parseColor("#1E1E1E"))
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
        }
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        btnCancelar.setOnClickListener { bottomSheetDialog.dismiss() }

        btnGuardar.setOnClickListener {
            val montoText = inputMonto.text.toString()
            if (montoText.isNotEmpty()) {
                val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(viewModel.fechaBase.time)
                viewModel.agregarTransaccion(
                    montoText.toDouble(),
                    tipoTransaccion,
                    spinner.selectedItem?.toString() ?: "General",
                    fechaRegistro,
                    inputDesc.text.toString()
                )
                bottomSheetDialog.dismiss()
            } else {
                inputMonto.error = "Por favor ingresa un monto"
            }
        }

        bottomSheetDialog.show()
    }

    private fun actualizarGrafico(lista: List<CategoriaDesglose>) {
        if (lista.isEmpty()) {
            pieChart.visibility = View.GONE
            return
        }
        pieChart.visibility = View.VISIBLE

        val entries = ArrayList<PieEntry>()
        val coloresCustomizados = ArrayList<Int>()
        var sumaTotal = 0.0

        for (item in lista) {
            entries.add(PieEntry(item.monto.toFloat(), item.nombre))
            sumaTotal += item.monto
            try {
                coloresCustomizados.add(Color.parseColor(item.colorHex))
            } catch (e: Exception) {
                coloresCustomizados.add(Color.GRAY)
            }
        }

        totalActual = sumaTotal

        val dataSet = PieDataSet(entries, "").apply {
            colors = coloresCustomizados
            setDrawValues(false)
            sliceSpace = 2f
            selectionShift = 5f
        }

        pieChart.centerText = "Total\n$ ${String.format(Locale.US, "%.2f", sumaTotal)}"
        pieChart.data = PieData(dataSet)
        pieChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
    }

    private fun setupPieChart() {
        pieChart.apply {
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setCenterTextColor(android.graphics.Color.WHITE)
            setCenterTextSize(18f)

            holeRadius = 75f
            transparentCircleRadius = 75f

            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            setExtraOffsets(5f, 5f, 5f, 5f)

            setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                    if (e is PieEntry) {
                        val categoriaSeleccionada = e.label
                        val montoSeleccionado = e.value
                        centerText = "$categoriaSeleccionada\n$ ${String.format(Locale.US, "%.2f", montoSeleccionado)}"
                    }
                }

                override fun onNothingSelected() {
                    centerText = "Total\n$ ${String.format(Locale.US, "%.2f", totalActual)}"
                }
            })
        }
    }
}