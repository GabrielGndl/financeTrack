package com.example.financetrack.data.model

data class Category(
    val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val color: String, // Guardaremos el código Hexadecimal (Ej. "#FF0000")
    val icono: Int     // Guardaremos el ID del ícono de Android
)