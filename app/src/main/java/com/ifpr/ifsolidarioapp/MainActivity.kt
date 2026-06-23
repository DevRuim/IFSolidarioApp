package com.ifpr.ifsolidarioapp

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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navView: BottomNavigationView =
            binding.navView

        val navController =
            findNavController(R.id.nav_host_fragment_activity_main)

        if (intent.getBooleanExtra("abrirPerfil", false)) {

            navController.navigate(
                R.id.navigation_profile
            )
        }

        // REMOVEU setupActionBarWithNavController

        navView.setupWithNavController(navController)

        carregarTipoUsuario(navView)
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

        when (tipo) {

            "ONG" -> {

                menu.findItem(R.id.navigation_dashboard)
                    .isVisible = false

                menu.findItem(R.id.navigation_notifications)
                    .isVisible = true
            }

            "Doador" -> {

                menu.findItem(R.id.navigation_dashboard)
                    .isVisible = true

                menu.findItem(R.id.navigation_notifications)
                    .isVisible = false
            }

            "VISITANTE" -> {

                menu.findItem(R.id.navigation_dashboard)
                    .isVisible = false

                menu.findItem(R.id.navigation_notifications)
                    .isVisible = false

                // opcionalmente esconder perfil também
                // menu.findItem(R.id.navigation_profile).isVisible = false
            }
        }
    }
}
