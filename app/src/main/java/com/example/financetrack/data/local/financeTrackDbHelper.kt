package com.example.financetrack.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.financetrack.data.model.Transaction

class FinanceTrackDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "FinanceTrack.db"

        // Nombre de la tabla y columnas
        const val TABLE_TRANSACTIONS = "transactions"
        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_TYPE = "type"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_DESCRIPTION = "description"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Sentencia SQL para crear la tabla
        val createTableQuery = ("CREATE TABLE $TABLE_TRANSACTIONS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_AMOUNT REAL,"
                + "$COLUMN_TYPE TEXT,"
                + "$COLUMN_CATEGORY TEXT,"
                + "$COLUMN_DATE TEXT,"
                + "$COLUMN_DESCRIPTION TEXT)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

    // --- OPERACIONES CRUD BÁSICAS ---

    // 1. Crear (Insertar) una nueva transacción
    fun insertTransaction(transaction: Transaction): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_TYPE, transaction.type)
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_DESCRIPTION, transaction.description)
        }
        val success = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return success // Retorna el ID de la fila insertada o -1 si hubo error
    }

    // 2. Leer (Obtener) todas las transacciones
    fun getAllTransactions(): List<Transaction> {
        val transactionList = ArrayList<Transaction>()
        val selectQuery = "SELECT * FROM $TABLE_TRANSACTIONS ORDER BY $COLUMN_DATE DESC" // Las más recientes primero
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val transaction = Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                )
                transactionList.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return transactionList
    }
}