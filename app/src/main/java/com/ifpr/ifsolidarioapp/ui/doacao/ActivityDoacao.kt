package com.ifpr.ifsolidarioapp.ui.doacao

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.ui.dashboard.DashboardFragment

class ActivityDoacao : AppCompatActivity() {

    private val fragments = mutableListOf<DashboardFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_doacao)

        val finalizar = findViewById<Button>(R.id.button_finalizar)

        finalizar.setOnClickListener {
            salvarTodosItens()
        }

        adicionarNovoFragment()
    }

    fun adicionarNovoFragment() {

        val fragment = DashboardFragment()

        fragments.add(fragment)

        supportFragmentManager.beginTransaction()
            .add(R.id.container_fragments, fragment)
            .commit()
    }

    private fun salvarTodosItens() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val database = FirebaseDatabase.getInstance().reference

        for (fragment in fragments) {

            val item = fragment.obterItem()

            if (item != null) {

                val key = database.child("itens")
                    .child(uid)
                    .push()
                    .key!!

                database.child("itens")
                    .child(uid)
                    .child(key)
                    .setValue(item)
            }
        }

    }
}