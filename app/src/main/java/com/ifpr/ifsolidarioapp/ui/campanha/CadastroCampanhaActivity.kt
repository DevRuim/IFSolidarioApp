package com.ifpr.ifsolidarioapp.ui.campanha

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Campanha


class CadastroCampanhaActivity  : AppCompatActivity() {
    private lateinit var textCadastroCampanhaTitle: TextView
    private lateinit var registerNameCampanhaEditText: EditText
    private lateinit var registerEnderecoEditText: EditText
    private lateinit var registerDescricaoEditText: EditText
    private lateinit var registerMetaEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sairButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_campanha)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        textCadastroCampanhaTitle = findViewById(R.id.textCadastroCampanhaTitle)
        registerNameCampanhaEditText = findViewById(R.id.registerNameCampanhaEditText)
        registerEnderecoEditText = findViewById(R.id.registerEnderecoEditText)
        registerDescricaoEditText = findViewById(R.id.registerDescricaoEditText)
        registerMetaEditText = findViewById(R.id.registerMetaEditText)
        registerButton = findViewById(R.id.salvarButton)
        sairButton = findViewById(R.id.sairButton)

        registerButton.setOnClickListener {
            createAccount()
        }

        sairButton.setOnClickListener {
            finish()
        }
    }

    private fun createAccount() {

        val nomeCampanha = registerNameCampanhaEditText.text.toString().trim()
        val endereco = registerEnderecoEditText.text.toString().trim()
        val descricao = registerDescricaoEditText.text.toString().trim()
        val metaTexto = registerMetaEditText.text.toString().trim()

        if (nomeCampanha.isEmpty() || endereco.isEmpty() || descricao.isEmpty() || metaTexto.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val meta = metaTexto.toDouble()

        // pega id do usuario logado
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // cria chave única no banco
        val campanhaKey = database.child("campanhas").push().key

        if (campanhaKey == null) {
            Toast.makeText(this, "Erro ao gerar chave", Toast.LENGTH_SHORT).show()
            return
        }

        val campanha = Campanha(
            key = campanhaKey,
            nomeCampanha = nomeCampanha,
            endereco = endereco,
            descricao = descricao,
            meta = meta,
            criadorId = userId
        )

        database.child("campanhas")
            .child(campanhaKey)
            .setValue(campanha)
            .addOnSuccessListener {

                Toast.makeText(this, "Campanha criada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()

            }
            .addOnFailureListener {

                Toast.makeText(this, "Erro ao salvar campanha", Toast.LENGTH_SHORT).show()
            }
    }
}