package com.example.financetrack.data.model

data class CategoriaDesglose(
    val nombre: String,
    val monto: Double,
    val porcentaje: Int,
    val colorHex: String = "#888888"
)