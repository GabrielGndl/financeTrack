package com.example.financetrack.ui.categorias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Category
import com.example.financetrack.data.repository.TransactionRepository

class CategoriasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(FinanceTrackDbHelper(application))

    private val _categoriasVisibles = MutableLiveData<List<Category>>()
    val categoriasVisibles: LiveData<List<Category>> = _categoriasVisibles

    fun cargarCategorias(esGasto: Boolean) {
        val tipo = if (esGasto) "GASTO" else "INGRESO"
        val lista = repository.getAllCategories().filter { it.tipo == tipo }
        _categoriasVisibles.value = lista
    }

    fun agregarCategoria(nombre: String, tipo: String, colorHex: String, iconResId: Int) {
        val nueva = Category(nombre = nombre, tipo = tipo, color = colorHex, icono = iconResId)
        repository.insertCategory(nueva)
        cargarCategorias(tipo == "GASTO")
    }

    fun editarCategoria(id: Int, nombre: String, tipo: String, colorHex: String, iconResId: Int) {
        val actualizada = Category(id = id, nombre = nombre, tipo = tipo, color = colorHex, icono = iconResId)
        repository.updateCategory(actualizada)
        cargarCategorias(tipo == "GASTO")
    }

    fun eliminarCategoria(id: Int, esGasto: Boolean) {
        repository.deleteCategory(id)
        cargarCategorias(esGasto)
    }
}