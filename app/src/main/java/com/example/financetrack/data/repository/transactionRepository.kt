package com.example.financetrack.data.repository

import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Category
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
    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM categories", null)

        if (cursor.moveToFirst()) {
            do {
                categories.add(Category(
                    id = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    tipo = cursor.getString(2),
                    color = cursor.getString(3),
                    icono = cursor.getInt(4)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }

    // NUEVA FUNCIÓN PARA GUARDAR CATEGORÍAS
    fun insertCategory(category: Category) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("nombre", category.nombre)
            put("tipo", category.tipo)
            put("color", category.color)
            put("icono", category.icono)
        }
        db.insert("categories", null, values)
    }

    fun updateTransaction(transaction: Transaction) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("amount", transaction.amount)
            put("category", transaction.category)
            put("description", transaction.description)
            put("type", transaction.type)
        }
        db.update("transactions", values, "id = ?", arrayOf(transaction.id.toString()))
    }

    fun deleteTransaction(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("transactions", "id = ?", arrayOf(id.toString()))
    }

}