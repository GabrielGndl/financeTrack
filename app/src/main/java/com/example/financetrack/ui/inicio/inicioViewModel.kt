package com.example.financetrack.ui.inicio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financetrack.data.repository.TransactionRepository

class InicioViewModel(private val repository: TransactionRepository) : ViewModel() {

    // Usamos LiveData para que la vista se actualice automáticamente cuando cambien los valores
    private val _saldoActual = MutableLiveData<Double>()
    val saldoActual: LiveData<Double> get() = _saldoActual

    private val _totalIngresos = MutableLiveData<Double>()
    val totalIngresos: LiveData<Double> get() = _totalIngresos

    private val _totalGastos = MutableLiveData<Double>()
    val totalGastos: LiveData<Double> get() = _totalGastos

    fun calcularResumen() {
        val transacciones = repository.getAllTransactions()

        var ingresos = 0.0
        var gastos = 0.0

        // Recorremos la lista y sumamos según el tipo
        for (t in transacciones) {
            if (t.type == "INGRESO") {
                ingresos += t.amount
            } else if (t.type == "GASTO") {
                gastos += t.amount
            }
        }

        val saldo = ingresos - gastos

        // Actualizamos los valores
        _totalIngresos.value = ingresos
        _totalGastos.value = gastos
        _saldoActual.value = saldo
    }
}

class InicioViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InicioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InicioViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}