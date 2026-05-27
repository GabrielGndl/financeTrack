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

    // Memoria para saber qué estamos viendo y poder recargar la misma pantalla
    private var filtroEsGastoActual = true
    private var filtroPeriodoActual = "Mes"

    // Variables para controlar la fecha actual que estamos viendo
    val fechaBase: Calendar = Calendar.getInstance()
    private val _textoFecha = MutableLiveData<String>()
    val textoFecha: LiveData<String> = _textoFecha

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Navegar en el tiempo
    fun cambiarFecha(offset: Int, periodo: String, esGasto: Boolean) {
        when (periodo) {
            "Día" -> fechaBase.add(Calendar.DAY_OF_YEAR, offset)
            "Semana" -> fechaBase.add(Calendar.WEEK_OF_YEAR, offset)
            "Mes" -> fechaBase.add(Calendar.MONTH, offset)
            "Año" -> fechaBase.add(Calendar.YEAR, offset)
        }
        filtrarDatos(esGasto, periodo)
    }

    // Elegir fecha exacta desde el calendario
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
        // 1. Guardamos en la memoria lo que el usuario está viendo ahora mismo
        filtroEsGastoActual = esGasto
        filtroPeriodoActual = periodo

        // 2. Actualizamos el texto de la fecha en la interfaz
        actualizarTextoFecha(periodo)

        val todas = repository.getAllTransactions()

        // Traemos el diccionario de categorías
        val categoriasMap = repository.getAllCategories().associateBy { it.nombre }

        val tipoBusqueda = if (esGasto) "GASTO" else "INGRESO"
        val filtradasPorTipo = todas.filter { it.type == tipoBusqueda }

        val filtradasPorTiempo = filtradasPorTipo.filter { transaccion ->
            val fechaTransaccion = transaccion.date.let { sdf.parse(it) } ?: return@filter false
            val calTransaccion = java.util.Calendar.getInstance().apply { time = fechaTransaccion }

            when (periodo) {
                "Día" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR) && calTransaccion.get(java.util.Calendar.DAY_OF_YEAR) == fechaBase.get(java.util.Calendar.DAY_OF_YEAR)
                "Semana" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR) && calTransaccion.get(java.util.Calendar.WEEK_OF_YEAR) == fechaBase.get(java.util.Calendar.WEEK_OF_YEAR)
                "Mes" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR) && calTransaccion.get(java.util.Calendar.MONTH) == fechaBase.get(java.util.Calendar.MONTH)
                "Año" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR)
                else -> true
            }
        }.map { t ->
            // Inyectamos el color e ícono
            val cat = categoriasMap[t.category]
            t.copy(
                colorCategory = cat?.color ?: "#888888",
                iconCategory = cat?.icono ?: android.R.drawable.ic_menu_agenda
            )
        }.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.hora })

        _transaccionesFiltradas.value = filtradasPorTiempo

        val agrupadas = filtradasPorTiempo.groupBy { it.category }
        val totalGeneral = filtradasPorTiempo.sumOf { it.amount }

        val desglose = agrupadas.map { (cat, lista) ->
            val totalCat = lista.sumOf { it.amount }
            val porcentaje = if (totalGeneral > 0) (totalCat / totalGeneral * 100).toInt() else 0

            // NUEVO: Buscamos el color en el diccionario de categorías
            val colorCategoria = categoriasMap[cat]?.color ?: "#888888"

            com.example.financetrack.data.model.CategoriaDesglose(cat, totalCat, porcentaje, colorCategoria)
        }.sortedByDescending { it.monto }

        _datosGrafico.value = desglose
    }

    fun obtenerCategorias(tipo: String): List<Category> = repository.getAllCategories().filter { it.tipo == tipo }

    fun agregarTransaccion(monto: Double, tipo: String, categoria: String, fecha: String, descripcion: String) {
        // Capturamos la hora exacta en formato 24h
        val horaActual = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val nueva = Transaction(
            amount = monto,
            type = tipo,
            category = categoria,
            date = fecha,
            hora = horaActual, // Inyectamos la hora
            description = descripcion
        )
        repository.insertTransaction(nueva)

        // Recargar la lista automáticamente
        filtrarDatos(filtroEsGastoActual, filtroPeriodoActual)
    }

    fun editarTransaccion(id: Int, monto: Double, tipo: String, categoria: String, fecha: String, descripcion: String) {
        val horaActual = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val actualizada = Transaction(
            id = id,
            amount = monto,
            type = tipo,
            category = categoria,
            date = fecha,
            hora = horaActual, // Actualizamos la hora para que el cambio suba arriba
            description = descripcion
        )
        repository.updateTransaction(actualizada)

        // Recargar la lista usando la memoria
        filtrarDatos(filtroEsGastoActual, filtroPeriodoActual)
    }

    fun eliminarTransaccion(id: Int, esGasto: Boolean) {
        repository.deleteTransaction(id)

        // Recargar la lista usando la memoria
        filtrarDatos(filtroEsGastoActual, filtroPeriodoActual)
    }
}