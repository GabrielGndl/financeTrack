package com.example.financetrack.ui.ingresos

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.financetrack.R
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.repository.TransactionRepository

class IngresosFragment : Fragment(R.layout.fragment_ingresos) {

    private lateinit var viewModel: IngresosViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = FinanceTrackDbHelper(requireContext())
        val repository = TransactionRepository(dbHelper)
        val factory = IngresosViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[IngresosViewModel::class.java]

        val etMonto = view.findViewById<EditText>(R.id.etMontoIngreso)
        val etCategoria = view.findViewById<EditText>(R.id.etCategoriaIngreso)
        val etFecha = view.findViewById<EditText>(R.id.etFechaIngreso)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcionIngreso)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarIngreso)

        btnGuardar.setOnClickListener {
            val montoStr = etMonto.text.toString()
            val categoria = etCategoria.text.toString()
            val fecha = etFecha.text.toString()
            val descripcion = etDescripcion.text.toString()

            if (montoStr.isNotEmpty() && categoria.isNotEmpty() && fecha.isNotEmpty()) {
                val monto = montoStr.toDouble()
                viewModel.guardarIngreso(monto, categoria, fecha, descripcion)

                Toast.makeText(requireContext(), "Ingreso registrado offline", Toast.LENGTH_SHORT).show()

                etMonto.text.clear()
                etCategoria.text.clear()
                etFecha.text.clear()
                etDescripcion.text.clear()
            } else {
                Toast.makeText(requireContext(), "Completa monto, categoría y fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }
}