package com.example.financetrack.ui.gastos

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

class GastosFragment : Fragment(R.layout.fragment_gastos) {

    private lateinit var viewModel: GastosViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Preparar la conexión local (Offline-first)
        val dbHelper = FinanceTrackDbHelper(requireContext())
        val repository = TransactionRepository(dbHelper)
        val factory = GastosViewModelFactory(repository)

        // 2. Instanciar el ViewModel
        viewModel = ViewModelProvider(this, factory)[GastosViewModel::class.java]

        // 3. Vincular los elementos visuales del XML
        val etMonto = view.findViewById<EditText>(R.id.etMonto)
        val etCategoria = view.findViewById<EditText>(R.id.etCategoria)
        val etFecha = view.findViewById<EditText>(R.id.etFecha)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)

        // 4. Configurar lo que pasa al presionar "Guardar Gasto"
        btnGuardar.setOnClickListener {
            val montoStr = etMonto.text.toString()
            val categoria = etCategoria.text.toString()
            val fecha = etFecha.text.toString()
            val descripcion = etDescripcion.text.toString()

            // Validar que los campos importantes no estén vacíos
            if (montoStr.isNotEmpty() && categoria.isNotEmpty() && fecha.isNotEmpty()) {
                val monto = montoStr.toDouble()

                // Enviar la información al ViewModel para que la guarde
                viewModel.guardarGasto(monto, categoria, fecha, descripcion)

                // Mostrar un mensaje de éxito rápido
                Toast.makeText(requireContext(), "Gasto registrado offline", Toast.LENGTH_SHORT).show()

                // Limpiar el formulario para el próximo registro rápido
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