package com.example.financetrack.data.repository

import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Transaction

class TransactionRepository(private val dbHelper: FinanceTrackDbHelper) {

    // Función para pedirle a SQLite TODAS las transacciones
    fun getAllTransactions(): List<Transaction> {
        return dbHelper.getAllTransactions()
    }

    // Función para mandar a SQLite una nueva transacción a guardar
    fun insertTransaction(transaction: Transaction): Boolean {
        val result = dbHelper.insertTransaction(transaction)
        // Si result es diferente de -1, significa que se guardó con éxito
        return result != -1L
    }
}