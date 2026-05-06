package com.example.financetrack.ui.categorias

import androidx.lifecycle.ViewModel

class CategoriasViewModel : ViewModel() {

    // Función que la Vista llamará para guardar una nueva categoría
    fun agregarCategoria(nombre: String, tipo: String) {
        // TODO: Más adelante conectaremos esto a una nueva tabla en SQLite
        // Por ahora, simulamos que procesamos la información
        println("Nueva categoría registrada: $nombre de tipo $tipo")
    }

    fun eliminarCategoria(nombre: String) {
        // TODO: Lógica para eliminar de la base de datos
        println("Categoría eliminada: $nombre")
    }
}