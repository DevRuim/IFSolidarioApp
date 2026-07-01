package com.ifpr.ifsolidarioapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.navigation.ui.setupWithNavController
import com.ifpr.ifsolidarioapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        carregarTipoUsuario(navView)

        // Trata intent inicial (ex: vindo do ConquistaActivity)
        tratarIntentDeNavegacao(intent, navView)
    }

    /**
     * Chamado quando a MainActivity já existe em memória e recebe um novo
     * Intent via FLAG_ACTIVITY_SINGLE_TOP (vindo do ConquistaActivity).
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { tratarIntentDeNavegacao(it, binding.navView) }
    }

    private fun tratarIntentDeNavegacao(intent: Intent, navView: BottomNavigationView) {
        when {
            intent.getBooleanExtra("abrirPerfil",  false) -> {
                navView.selectedItemId = R.id.navigation_profile
            }
            intent.getBooleanExtra("abrirRanking", false) -> {
                navView.selectedItemId = R.id.navigation_ranking
            }
        }
    }

    /**
     * Chamado pelo DoacaoFragment após limpar a pilha de navegação.
     * Seleciona o Ranking no BottomNav, mantendo Home → Ranking na pilha
     * e o BottomNav sincronizado (Home fica acessível).
     */
    fun selecionarRanking() {
        binding.navView.selectedItemId = R.id.navigation_ranking
    }

    private fun carregarTipoUsuario(navView: BottomNavigationView) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            configurarMenu(navView.menu, "VISITANTE")
            return
        }

        val uid         = user.uid
        val usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
        val ongsRef     = FirebaseDatabase.getInstance().getReference("ongs").child(uid)

        usuariosRef.get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    configurarMenu(navView.menu, "Doador")

                } else {
                    ongsRef.get().addOnSuccessListener { ongSnap ->
                        configurarMenu(navView.menu, if (ongSnap.exists()) "ONG" else "VISITANTE")
                    }
                }
            }
            .addOnFailureListener {

                configurarMenu(navView.menu, "VISITANTE")
                Toast.makeText(this, "Erro ao carregar usuário", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarMenu(menu: Menu, tipo: String?) {
        menu.findItem(R.id.navigation_ranking).isVisible = true
        menu.findItem(R.id.navigation_notifications).isVisible = (tipo == "ONG")
    }
}
