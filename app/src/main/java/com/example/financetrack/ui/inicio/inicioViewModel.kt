package com.example.financetrack.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.CategoriaDesglose
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.data.repository.TransactionRepository

class InicioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(FinanceTrackDbHelper(application))

    private val _saldoDisponible = MutableLiveData<Double>()
    val saldoDisponible: LiveData<Double> = _saldoDisponible

    private val _totalIngresos = MutableLiveData<Double>()
    val totalIngresos: LiveData<Double> = _totalIngresos

    private val _totalGastos = MutableLiveData<Double>()
    val totalGastos: LiveData<Double> = _totalGastos

    // LiveData para los datos del gráfico
    private val _datosGrafico = MutableLiveData<List<CategoriaDesglose>>()
    val datosGrafico: LiveData<List<CategoriaDesglose>> = _datosGrafico

    // LiveData para las transacciones recientes (Faltaba declararlo en tu código)
    private val _transaccionesRecientes = MutableLiveData<List<Transaction>>()
    val transaccionesRecientes: LiveData<List<Transaction>> = _transaccionesRecientes

    fun cargarResumen() {
        val todasLasTransacciones = repository.getAllTransactions()
        val categoriasMap = repository.getAllCategories().associateBy { it.nombre }

        val ingresos = todasLasTransacciones.filter { it.type == "INGRESO" }.sumOf { it.amount }
        val gastos = todasLasTransacciones.filter { it.type == "GASTO" }.sumOf { it.amount }

        _totalIngresos.value = ingresos
        _totalGastos.value = gastos
        _saldoDisponible.value = ingresos - gastos

// 1. Preparar datos para el Gráfico Circular (Ingresos vs Gastos)
        val listaDesglose = mutableListOf<CategoriaDesglose>()

        if (ingresos > 0) {
            listaDesglose.add(CategoriaDesglose("Ingresos", ingresos, 0))
        }
        if (gastos > 0) {
            listaDesglose.add(CategoriaDesglose("Gastos", gastos, 0))
        }
        _datosGrafico.value = listaDesglose

// 2. Transacciones recientes con sus colores
        val recientes = todasLasTransacciones
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.hora })
            .take(5)
            .map { t ->
                val cat = categoriasMap[t.category]
                t.copy(
                    colorCategory = cat?.color ?: "#888888",
                    iconCategory = cat?.icono ?: android.R.drawable.ic_menu_agenda
                )
            }
        _transaccionesRecientes.value = recientes
    }
}