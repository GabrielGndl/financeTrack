package com.example.financetrack.ui.inicio

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrack.R
import com.example.financetrack.data.model.CategoriaDesglose
import com.example.financetrack.ui.transacciones.TransaccionesAdapter
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class InicioFragment : Fragment() {

    private lateinit var viewModel: InicioViewModel
    private lateinit var adapter: TransaccionesAdapter
    private lateinit var pieChart: PieChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[InicioViewModel::class.java]

        // Enlazar Vistas de Textos
        val tvSaldo = view.findViewById<TextView>(R.id.tvSaldoDisponible)
        val tvIngresos = view.findViewById<TextView>(R.id.tvTotalIngresos)
        val tvGastos = view.findViewById<TextView>(R.id.tvTotalGastos)
        pieChart = view.findViewById(R.id.pieChartInicio)

        // Configuración estética del gráfico circular
        setupPieChart()

        // Configurar la lista reciclada usando el adaptador de Transacciones
        val rvReciente = view.findViewById<RecyclerView>(R.id.rvActividadReciente)
        rvReciente.layoutManager = LinearLayoutManager(requireContext())

        adapter = TransaccionesAdapter(emptyList()) { _ ->
            // Al hacer clic en el inicio, le sugerimos ir a la pestaña específica
            Toast.makeText(requireContext(), "Ve a 'Transacciones' para editar o borrar", Toast.LENGTH_SHORT).show()
        }
        rvReciente.adapter = adapter

        // Observadores de Saldos
        viewModel.saldoDisponible.observe(viewLifecycleOwner) { saldo ->
            tvSaldo.text = String.format("$ %.2f", saldo)
        }

        viewModel.totalIngresos.observe(viewLifecycleOwner) { ingresos ->
            tvIngresos.text = String.format("$ %.2f", ingresos)
        }

        viewModel.totalGastos.observe(viewLifecycleOwner) { gastos ->
            tvGastos.text = String.format("$ %.2f", gastos)
        }

        // Observador de la Lista Reciente
        viewModel.transaccionesRecientes.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)
        }

        // Observador del Gráfico
        viewModel.datosGrafico.observe(viewLifecycleOwner) { listaDesglose ->
            actualizarGrafico(listaDesglose)
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setCenterTextColor(Color.WHITE)
            centerText = "Balance" // Cambiamos el texto central
            setCenterTextSize(18f)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = Color.WHITE
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            setEntryLabelColor(Color.WHITE)
            animateY(1000)
        }
    }

    private fun actualizarGrafico(lista: List<CategoriaDesglose>) {
        if (lista.isEmpty()) {
            pieChart.visibility = View.GONE
            return
        }
        pieChart.visibility = View.VISIBLE

        val entries = ArrayList<PieEntry>()
        val colores = ArrayList<Int>()

        // Asignamos los datos y forzamos los colores
        for (item in lista) {
            entries.add(PieEntry(item.monto.toFloat(), item.nombre))

            if (item.nombre == "Ingresos") {
                colores.add(Color.parseColor("#4CAF50")) // Verde Material Design
            } else {
                colores.add(Color.parseColor("#F44336")) // Rojo Material Design
            }
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = colores // Aplicamos nuestra lista de colores verde/rojo
            valueTextColor = Color.WHITE
            valueTextSize = 14f
            // Opcional: Para que los números se vean mejor fuera de las rebanadas pequeñas
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.2f
            valueLinePart2Length = 0.4f
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate() // Refrescar el gráfico
    }

    // Usamos onResume para que los datos se recarguen cada vez que el usuario vuelve a esta pestaña
    override fun onResume() {
        super.onResume()
        viewModel.cargarResumen()
    }
}