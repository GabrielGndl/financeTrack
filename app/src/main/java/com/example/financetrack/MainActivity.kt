package com.example.financetrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.financetrack.ui.categorias.CategoriasFragment
import com.example.financetrack.ui.gastos.GastosFragment
import com.example.financetrack.ui.ingresos.IngresosFragment
import com.example.financetrack.ui.inicio.InicioFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forzar Modo Claro para que la app siempre mantenga el diseño original
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Cargar el fragment de Inicio por defecto al abrir la app
        if (savedInstanceState == null) {
            replaceFragment(InicioFragment())
            bottomNavigation.selectedItemId = R.id.nav_inicio
        }

        // Configurar el "escuchador" de clics del menú inferior
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    replaceFragment(InicioFragment())
                    true
                }
                R.id.nav_gastos -> {
                    replaceFragment(GastosFragment())
                    true
                }
                R.id.nav_ingresos -> {
                    replaceFragment(IngresosFragment())
                    true
                }
                R.id.nav_categorias -> {
                    replaceFragment(CategoriasFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Función auxiliar para cambiar el Fragment en el contenedor principal
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}