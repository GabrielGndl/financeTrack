package com.example.financetrack.ui.transacciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Category
import com.example.financetrack.data.model.CategoriaDesglose
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.data.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TransaccionesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(FinanceTrackDbHelper(application))

    private val _transaccionesFiltradas = MutableLiveData<List<Transaction>>()
    val transaccionesFiltradas: LiveData<List<Transaction>> = _transaccionesFiltradas

    private val _datosGrafico = MutableLiveData<List<CategoriaDesglose>>()
    val datosGrafico: LiveData<List<CategoriaDesglose>> = _datosGrafico

    // NUEVO: Variables para controlar la fecha actual que estamos viendo
    val fechaBase: Calendar = Calendar.getInstance()
    private val _textoFecha = MutableLiveData<String>()
    val textoFecha: LiveData<String> = _textoFecha

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // NUEVO: Navegar en el tiempo
    fun cambiarFecha(offset: Int, periodo: String, esGasto: Boolean) {
        when (periodo) {
            "Día" -> fechaBase.add(Calendar.DAY_OF_YEAR, offset)
            "Semana" -> fechaBase.add(Calendar.WEEK_OF_YEAR, offset)
            "Mes" -> fechaBase.add(Calendar.MONTH, offset)
            "Año" -> fechaBase.add(Calendar.YEAR, offset)
        }
        filtrarDatos(esGasto, periodo)
    }

    // NUEVO: Elegir fecha exacta desde el calendario
    fun setFechaExacta(year: Int, month: Int, day: Int, periodo: String, esGasto: Boolean) {
        fechaBase.set(year, month, day)
        filtrarDatos(esGasto, periodo)
    }

    private fun actualizarTextoFecha(periodo: String) {
        val formato = when (periodo) {
            "Día" -> SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
            "Semana" -> SimpleDateFormat("'Semana del' dd MMM", Locale("es", "ES"))
            "Mes" -> SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            "Año" -> SimpleDateFormat("yyyy", Locale("es", "ES"))
            else -> SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        }
        _textoFecha.value = formato.format(fechaBase.time).replaceFirstChar { it.uppercase() }
    }

    fun filtrarDatos(esGasto: Boolean, periodo: String) {
        actualizarTextoFecha(periodo) // Actualizamos la etiqueta de la fecha visual

        val todas = repository.getAllTransactions()
        val tipoBusqueda = if (esGasto) "GASTO" else "INGRESO"
        val filtradasPorTipo = todas.filter { it.type == tipoBusqueda }

        val filtradasPorTiempo = filtradasPorTipo.filter { transaccion ->
            val fechaTransaccion = transaccion.date?.let { sdf.parse(it) } ?: return@filter false
            val calTransaccion = Calendar.getInstance().apply { time = fechaTransaccion }

            // Comparamos contra nuestra 'fechaBase', no contra 'hoy'
            when (periodo) {
                "Día" -> {
                    calTransaccion.get(Calendar.YEAR) == fechaBase.get(Calendar.YEAR) &&
                            calTransaccion.get(Calendar.DAY_OF_YEAR) == fechaBase.get(Calendar.DAY_OF_YEAR)
                }
                "Semana" -> {
                    calTransaccion.get(Calendar.YEAR) == fechaBase.get(Calendar.YEAR) &&
                            calTransaccion.get(Calendar.WEEK_OF_YEAR) == fechaBase.get(Calendar.WEEK_OF_YEAR)
                }
                "Mes" -> {
                    calTransaccion.get(Calendar.YEAR) == fechaBase.get(Calendar.YEAR) &&
                            calTransaccion.get(Calendar.MONTH) == fechaBase.get(Calendar.MONTH)
                }
                "Año" -> {
                    calTransaccion.get(Calendar.YEAR) == fechaBase.get(Calendar.YEAR)
                }
                else -> true
            }
        }.sortedByDescending { it.date }

        _transaccionesFiltradas.value = filtradasPorTiempo

        val agrupadas = filtradasPorTiempo.groupBy { it.category }
        val totalGeneral = filtradasPorTiempo.sumOf { it.amount }

        val desglose = agrupadas.map { (cat, lista) ->
            val totalCat = lista.sumOf { it.amount }
            val porcentaje = if (totalGeneral > 0) (totalCat / totalGeneral * 100).toInt() else 0
            CategoriaDesglose(cat ?: "General", totalCat, porcentaje)
        }.sortedByDescending { it.monto }

        _datosGrafico.value = desglose
    }

    fun obtenerCategorias(tipo: String): List<Category> = repository.getAllCategories().filter { it.tipo == tipo }

    fun agregarTransaccion(monto: Double, tipo: String, categoria: String, fecha: String, descripcion: String) {
        val nueva = Transaction(amount = monto, type = tipo, category = categoria, date = fecha, description = descripcion)
        repository.insertTransaction(nueva)
        filtrarDatos(tipo == "GASTO", "Mes") // Refrescar
    }

    fun editarTransaccion(id: Int, monto: Double, tipo: String, categoria: String, fecha: String, descripcion: String) {
        val actualizada = Transaction(id = id, amount = monto, type = tipo, category = categoria, date = fecha, description = descripcion)
        repository.updateTransaction(actualizada)
        filtrarDatos(tipo == "GASTO", "Mes")
    }

    fun eliminarTransaccion(id: Int, esGasto: Boolean) {
        repository.deleteTransaction(id)
        filtrarDatos(esGasto, "Mes")
    }
}