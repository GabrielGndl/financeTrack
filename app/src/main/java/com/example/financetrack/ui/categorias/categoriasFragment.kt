package com.example.financetrack.ui.categorias

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.financetrack.R

class CategoriasFragment : Fragment(R.layout.fragment_categorias) {

    private lateinit var viewModel: CategoriasViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instanciar el ViewModel (Esta vez sin Factory porque aún no le pasamos el Repositorio)
        viewModel = ViewModelProvider(this)[CategoriasViewModel::class.java]

        val etNombre = view.findViewById<EditText>(R.id.etNombreCategoria)
        val rgTipo = view.findViewById<RadioGroup>(R.id.rgTipoCategoria)
        val btnAgregar = view.findViewById<Button>(R.id.btnAgregarCategoria)

        btnAgregar.setOnClickListener {
            val nombre = etNombre.text.toString()

            // Averiguar qué RadioButton está seleccionado (Ingreso o Gasto)
            val selectedId = rgTipo.checkedRadioButtonId
            val rbSeleccionado = view.findViewById<RadioButton>(selectedId)
            val tipo = rbSeleccionado.text.toString().uppercase() // "INGRESO" o "GASTO"

            if (nombre.isNotEmpty()) {
                viewModel.agregarCategoria(nombre, tipo)

                Toast.makeText(requireContext(), "Categoría '$nombre' agregada", Toast.LENGTH_SHORT).show()

                etNombre.text.clear()
                // Volvemos al valor por defecto (Gasto)
                rgTipo.check(R.id.rbGasto)
            } else {
                Toast.makeText(requireContext(), "Ingresa un nombre para la categoría", Toast.LENGTH_SHORT).show()
            }
        }
    }
}