package com.example.financetrack.ui.ingresos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.data.repository.TransactionRepository

class IngresosViewModel(private val repository: TransactionRepository) : ViewModel() {

    fun guardarIngreso(monto: Double, categoria: String, fecha: String, descripcion: String) {
        val nuevoIngreso = Transaction(
            amount = monto,
            type = "INGRESO", // Aquí está la diferencia clave
            category = categoria,
            date = fecha,
            description = descripcion
        )
        repository.insertTransaction(nuevoIngreso)
    }
}

class IngresosViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngresosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngresosViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}