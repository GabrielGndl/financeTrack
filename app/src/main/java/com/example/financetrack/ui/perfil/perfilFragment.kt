package com.example.financetrack.ui.perfil

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.financetrack.R
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.repository.TransactionRepository
import com.example.financetrack.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.concurrent.thread

class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 1. Enlazamos los nuevos componentes (LinearLayouts en lugar de Buttons)
        val tvUsuarioCorreo = view.findViewById<TextView>(R.id.tvUsuarioCorreo)
        val btnRespaldar = view.findViewById<LinearLayout>(R.id.btnRespaldar)
        val btnSincronizar = view.findViewById<LinearLayout>(R.id.btnSincronizar)
        val btnCerrarSesion = view.findViewById<LinearLayout>(R.id.btnCerrarSesion)

        // 2. Extraemos los TextViews internos para cambiar sus textos durante la carga
        val txtRespaldar = btnRespaldar.getChildAt(1) as TextView
        val txtSincronizar = btnSincronizar.getChildAt(1) as TextView

        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvUsuarioCorreo.text = currentUser.email
        } else {
            tvUsuarioCorreo.text = "Usuario no identificado"
        }

        // ==========================================
        // 1. LÓGICA DE RESPALDO (SUBIR A LA NUBE)
        // ==========================================
        btnRespaldar.setOnClickListener {
            val user = auth.currentUser ?: return@setOnClickListener

            // Bloqueamos el botón y cambiamos el texto
            btnRespaldar.isEnabled = false
            txtRespaldar.text = "Subiendo a la nube..."
            Toast.makeText(requireContext(), "Iniciando respaldo...", Toast.LENGTH_SHORT).show()

            val repository = TransactionRepository(FinanceTrackDbHelper(requireContext()))
            val firestoreDb = FirebaseFirestore.getInstance()
            val uid = user.uid

            thread {
                try {
                    val transacciones = repository.getAllTransactions()
                    val categorias = repository.getAllCategories()

                    for (t in transacciones) {
                        val transaccionMap = hashMapOf(
                            "amount" to t.amount,
                            "type" to t.type,
                            "category" to t.category,
                            "date" to t.date,
                            "hora" to t.hora,
                            "description" to t.description
                        )
                        firestoreDb.collection("usuarios").document(uid)
                            .collection("transacciones").document(t.id.toString())
                            .set(transaccionMap)
                    }

                    for (c in categorias) {
                        val categoriaMap = hashMapOf(
                            "nombre" to c.nombre,
                            "tipo" to c.tipo,
                            "color" to c.color,
                            "icono" to c.icono
                        )
                        firestoreDb.collection("usuarios").document(uid)
                            .collection("categorias").document(c.id.toString())
                            .set(categoriaMap)
                    }

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "¡Datos respaldados con éxito!", Toast.LENGTH_LONG).show()
                        // Restauramos el botón
                        btnRespaldar.isEnabled = true
                        txtRespaldar.text = "Respaldar datos en la nube"
                    }

                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        btnRespaldar.isEnabled = true
                        txtRespaldar.text = "Respaldar datos en la nube"
                    }
                }
            }
        }

        // ==========================================
        // 2. LÓGICA DE DESCARGA (SINCRONIZAR DESDE LA NUBE)
        // ==========================================
        btnSincronizar.setOnClickListener {
            val user = auth.currentUser ?: return@setOnClickListener

            btnSincronizar.isEnabled = false
            txtSincronizar.text = "Descargando..."
            Toast.makeText(requireContext(), "Obteniendo datos de la nube...", Toast.LENGTH_SHORT).show()

            val firestoreDb = FirebaseFirestore.getInstance()
            val uid = user.uid
            val dbHelper = FinanceTrackDbHelper(requireContext())

            firestoreDb.collection("usuarios").document(uid).collection("categorias").get()
                .addOnSuccessListener { categoriasSnapshot ->

                    firestoreDb.collection("usuarios").document(uid).collection("transacciones").get()
                        .addOnSuccessListener { transaccionesSnapshot ->

                            thread {
                                val db = dbHelper.writableDatabase
                                try {
                                    db.beginTransaction()

                                    db.execSQL("DELETE FROM transactions")
                                    db.execSQL("DELETE FROM categories")

                                    // Restaurar Categorías
                                    for (doc in categoriasSnapshot.documents) {
                                        val cValues = ContentValues().apply {
                                            put("id", doc.id.toIntOrNull())
                                            put("nombre", doc.getString("nombre"))
                                            put("tipo", doc.getString("tipo"))
                                            put("color", doc.getString("color"))
                                            put("icono", doc.getLong("icono")?.toInt() ?: 0)
                                        }
                                        db.insert("categories", null, cValues)
                                    }

                                    // Restaurar Transacciones
                                    for (doc in transaccionesSnapshot.documents) {
                                        val tValues = ContentValues().apply {
                                            put("id", doc.id.toIntOrNull())
                                            val montoSeguro = doc.get("amount")?.toString()?.toDoubleOrNull() ?: 0.0
                                            put("amount", montoSeguro)
                                            put("type", doc.getString("type"))
                                            put("category", doc.getString("category"))
                                            put("date", doc.getString("date"))
                                            put("hora", doc.getString("hora"))
                                            put("description", doc.getString("description"))
                                        }
                                        db.insert("transactions", null, tValues)
                                    }

                                    db.setTransactionSuccessful()

                                    requireActivity().runOnUiThread {
                                        Toast.makeText(requireContext(), "¡Sincronización completada! Datos restaurados.", Toast.LENGTH_LONG).show()
                                        btnSincronizar.isEnabled = true
                                        txtSincronizar.text = "Sincronizar desde la nube"
                                    }

                                } catch (e: Exception) {
                                    requireActivity().runOnUiThread {
                                        Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                                        btnSincronizar.isEnabled = true
                                        txtSincronizar.text = "Sincronizar desde la nube"
                                    }
                                } finally {
                                    db.endTransaction()
                                    db.close()
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error al bajar movimientos: ${e.message}", Toast.LENGTH_SHORT).show()
                            btnSincronizar.isEnabled = true
                            txtSincronizar.text = "Sincronizar desde la nube"
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al bajar categorías: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSincronizar.isEnabled = true
                    txtSincronizar.text = "Sincronizar desde la nube"
                }
        }

        // ==========================================
        // 3. LÓGICA DE CERRAR SESIÓN (LIMPIA LOCAL)
        // ==========================================
        btnCerrarSesion.setOnClickListener {
            val dbHelper = FinanceTrackDbHelper(requireContext())
            val db = dbHelper.writableDatabase
            try {
                db.execSQL("DELETE FROM transactions")
                db.execSQL("DELETE FROM categories")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.close()
            }

            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // Agregamos la flag para evitar que el usuario vuelva atrás con el botón de retroceso
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}