package com.ifpr.ifsolidarioapp.ui.ong

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
import com.google.firebase.database.DatabaseReference
import com.ifpr.ifsolidarioapp.R


class CadastroOngActivity  : AppCompatActivity() {
    private lateinit var textCadastroONGTitle: TextView
    private lateinit var registerOngNameEditText: EditText
    private lateinit var registerOngEmailEditText: EditText
    private lateinit var registerOngTelefoneEditText: EditText
    private lateinit var registerCNPJEditText: EditText
    private lateinit var registerOngPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sairButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_ong)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        textCadastroONGTitle = findViewById(R.id.textCadastroONGTitle)
        registerOngNameEditText = findViewById(R.id.registerOngNameEditText)
        registerOngEmailEditText = findViewById(R.id.registerOngEmailEditText)
        registerOngTelefoneEditText = findViewById(R.id.registerOngTelefoneEditText)
        registerCNPJEditText = findViewById(R.id.registerCNPJEditText)
        registerOngPasswordEditText = findViewById(R.id.registerOngPasswordEditText)
        registerConfirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText)
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
        val name = registerNameEditText.text.toString().trim()
        val email = registerEmailEditText.text.toString().trim()
        val password = registerPasswordEditText.text.toString().trim()
        val confirmPassword = registerConfirmPasswordEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Novo usuário cadastrado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        val user = auth.currentUser
                        updateProfile(user, name)
                        sendEmailVerification(user)
                    } else {
                        val errorMessage = task.exception?.message ?: "Erro desconhecido"
                        Log.e("FirebaseAuth", "Erro ao cadastrar usuário: $errorMessage")
                        Toast.makeText(
                            this,
                            "Falha ao cadastrar novo usuário: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (ex: Exception) {
            Log.e("FirebaseAuth", "Erro ao conectar com o Firebase", ex)
            Toast.makeText(
                this,
                "Falha ao conectar com o Firebase: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }


    }

    private fun sendEmailVerification(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Verification email sent to ${user.email}.",
                        Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(baseContext, "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateProfile(user: FirebaseUser?, displayName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Nome do usuario alterado com sucesso.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "Não foi possivel alterar o nome do usuario.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}