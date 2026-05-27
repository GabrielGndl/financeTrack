package com.example.financetrack.data.repository

import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Category
import com.example.financetrack.data.model.Transaction

class TransactionRepository(private val dbHelper: FinanceTrackDbHelper) {

    // --- OPERACIONES DE TRANSACCIONES ---

    fun getAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM transactions", null)

        if (cursor.moveToFirst()) {
            do {
                transactions.add(Transaction(
                    id = cursor.getInt(0),
                    amount = cursor.getDouble(1),
                    type = cursor.getString(2),
                    category = cursor.getString(3),
                    date = cursor.getString(4),
                    hora = cursor.getString(5), // Recuperamos la hora
                    description = cursor.getString(6)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    fun insertTransaction(transaction: Transaction): Boolean {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("amount", transaction.amount)
            put("type", transaction.type)
            put("category", transaction.category)
            put("date", transaction.date)
            put("hora", transaction.hora) // Guardamos la hora
            put("description", transaction.description)
        }
        val result = db.insert("transactions", null, values)
        return result != -1L
    }

    fun updateTransaction(transaction: Transaction) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("amount", transaction.amount)
            put("type", transaction.type)
            put("category", transaction.category)
            put("date", transaction.date)
            put("hora", transaction.hora) // Actualizamos la hora también
            put("description", transaction.description)
        }
        db.update("transactions", values, "id = ?", arrayOf(transaction.id.toString()))
    }

    fun deleteTransaction(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("transactions", "id = ?", arrayOf(id.toString()))
    }


    // --- OPERACIONES DE CATEGORÍAS ---

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

    fun updateCategory(category: Category) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("nombre", category.nombre)
            put("tipo", category.tipo)
            put("color", category.color)
            put("icono", category.icono)
        }
        db.update("categories", values, "id = ?", arrayOf(category.id.toString()))
    }

    fun deleteCategory(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("categories", "id = ?", arrayOf(id.toString()))
    }
}