package com.ifpr.ifsolidarioapp

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController =
            findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_profile
            )
        )

        setupActionBarWithNavController(
            navController,
            appBarConfiguration
        )

        navView.setupWithNavController(navController)

        carregarTipoUsuario(navView)
    }

    private fun carregarTipoUsuario(navView: BottomNavigationView) {

        val user = FirebaseAuth.getInstance().currentUser
            ?: return

        val uid = user.uid

        FirebaseDatabase
            .getInstance()
            .reference
            .child("usuarios")
            .child(uid)
            .child("tipo_usuario")
            .get()
            .addOnSuccessListener { snapshot ->

                val tipo =
                    snapshot.getValue(String::class.java)

                configurarMenu(navView.menu, tipo)
            }
    }

    private fun configurarMenu(
        menu: Menu,
        tipo: String?
    ) {
        if (tipo == "ONG") {
            menu.findItem(R.id.navigation_dashboard).isVisible = false
            menu.findItem(R.id.navigation_notifications).isVisible = true
        } else {
            menu.findItem(R.id.navigation_dashboard).isVisible = true
            menu.findItem(R.id.navigation_notifications).isVisible = false
        }
    }
}