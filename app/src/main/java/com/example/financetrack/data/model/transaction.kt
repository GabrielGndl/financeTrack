package com.example.financetrack.data.model

data class Transaction(
    val id: Int = 0,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val description: String = ""
)