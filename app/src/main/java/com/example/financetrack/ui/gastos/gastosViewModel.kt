package com.example.financetrack.ui.gastos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.data.repository.TransactionRepository

class GastosViewModel(private val repository: TransactionRepository) : ViewModel() {

    // Esta función será llamada desde la pantalla (UI) cuando el usuario presione "Guardar"
    fun guardarGasto(monto: Double, categoria: String, fecha: String, descripcion: String) {
        val nuevoGasto = Transaction(
            amount = monto,
            type = "GASTO", // Lo forzamos a "GASTO" porque estamos en este módulo
            category = categoria,
            date = fecha,
            description = descripcion
        )

        // Le pasamos el objeto al repositorio para que lo guarde en SQLite
        repository.insertTransaction(nuevoGasto)
    }
}

// --- FÁBRICA DEL VIEWMODEL ---
// Como nuestro ViewModel necesita recibir el "TransactionRepository" en su constructor,
// Android exige crear una "Factory" que le enseñe a la app cómo construir este ViewModel.
class GastosViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GastosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GastosViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}