package com.example.financetrack.ui.inicio

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.financetrack.R
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.repository.TransactionRepository

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private lateinit var viewModel: InicioViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = FinanceTrackDbHelper(requireContext())
        val repository = TransactionRepository(dbHelper)
        val factory = InicioViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[InicioViewModel::class.java]

        val tvSaldoActual = view.findViewById<TextView>(R.id.tvSaldoActual)
        val tvTotalIngresos = view.findViewById<TextView>(R.id.tvTotalIngresos)
        val tvTotalGastos = view.findViewById<TextView>(R.id.tvTotalGastos)

// Observamos los cambios en los datos para actualizar la UI en tiempo real
        viewModel.saldoActual.observe(viewLifecycleOwner) { saldo ->
            tvSaldoActual.text = String.format("$ %.2f", saldo)
        }

        viewModel.totalIngresos.observe(viewLifecycleOwner) { ingresos ->
            tvTotalIngresos.text = String.format("$ %.2f", ingresos)
        }

        viewModel.totalGastos.observe(viewLifecycleOwner) { gastos ->
            tvTotalGastos.text = String.format("$ %.2f", gastos)
        }
    }

    // Cada vez que la pantalla vuelva a ser visible, recalculamos por si se agregó un gasto nuevo
    override fun onResume() {
        super.onResume()
        viewModel.calcularResumen()
    }
}