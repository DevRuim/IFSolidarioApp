package com.ifpr.ifsolidarioapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.ifpr.ifsolidarioapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: androidx.navigation.NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navView: BottomNavigationView =
            binding.navView

        navController =
            findNavController(R.id.nav_host_fragment_activity_main)

        navView.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }

                R.id.navigation_ranking -> {
                    navController.navigate(R.id.navigation_ranking)
                    true
                }

                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile)
                    true
                }

                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }

                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->

            android.util.Log.d(
                "NAV_TEST",
                "Destino atual = ${destination.label}"
            )
        }

        carregarTipoUsuario(navView)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.getBooleanExtra("abrirPerfil", false)) {

            window.decorView.post {

                try {
                    val navController =
                        findNavController(R.id.nav_host_fragment_activity_main)

                    // evita crash se já estiver no destino
                    if (navController.currentDestination?.id != R.id.navigation_profile) {
                        navController.navigate(R.id.navigation_profile)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                    // fallback seguro (NUNCA deixa o app quebrar)
                    binding.navView.selectedItemId = R.id.navigation_profile
                }
            }
        }
    }

    private fun carregarTipoUsuario(navView: BottomNavigationView) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            configurarMenu(navView.menu, "VISITANTE")
            return
        }

        val uid = user.uid

        val usuariosRef =
            FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)

        val ongsRef =
            FirebaseDatabase.getInstance()
                .getReference("ongs")
                .child(uid)

        usuariosRef.get()
            .addOnSuccessListener { usuarioSnapshot ->

                if (usuarioSnapshot.exists()) {

                    configurarMenu(navView.menu, "Doador")

                } else {

                    ongsRef.get()
                        .addOnSuccessListener { ongSnapshot ->

                            if (ongSnapshot.exists()) {
                                configurarMenu(navView.menu, "ONG")
                            } else {
                                configurarMenu(navView.menu, "VISITANTE")
                            }
                        }
                }
            }
            .addOnFailureListener {

                configurarMenu(navView.menu, "VISITANTE")

                Toast.makeText(
                    this,
                    "Erro ao carregar usuário",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun configurarMenu(
        menu: Menu,
        tipo: String?
    ) {

        menu.findItem(R.id.navigation_ranking)
            .isVisible = true

        when (tipo) {

            "ONG" -> {

                menu.findItem(R.id.navigation_notifications)
                    .isVisible = true
            }

            "Doador" -> {

                menu.findItem(R.id.navigation_notifications)
                    .isVisible = false
            }

            "VISITANTE" -> {

                menu.findItem(R.id.navigation_notifications)
                    .isVisible = false
            }
        }
    }
}
