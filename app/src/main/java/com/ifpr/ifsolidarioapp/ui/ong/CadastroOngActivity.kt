package com.ifpr.ifsolidarioapp.ui.ong

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Ong

class CadastroOngActivity : AppCompatActivity() {

    private lateinit var registerOngNameEditText: EditText
    private lateinit var registerOngEmailEditText: EditText
    private lateinit var registerOngTelefoneEditText: EditText
    private lateinit var registerCNPJEditText: EditText
    private lateinit var registerOngPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText

    private lateinit var registerButton: Button
//    private lateinit var sairButton: Button

    private lateinit var imagemOng: ImageView
    private lateinit var escolherImagemButton: FloatingActionButton

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    companion object {
        const val PICK_IMAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_ong)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        registerOngNameEditText = findViewById(R.id.registerOngNameEditText)
        registerOngEmailEditText = findViewById(R.id.registerOngEmailEditText)
        registerOngTelefoneEditText = findViewById(R.id.registerOngTelefoneEditText)
        registerCNPJEditText = findViewById(R.id.registerCNPJEditText)
        registerOngPasswordEditText = findViewById(R.id.registerOngPasswordEditText)
        registerConfirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText)
        registerButton = findViewById(R.id.salvarButton)
//        sairButton = findViewById(R.id.sairButton)

        imagemOng = findViewById(R.id.imagemOng)
        escolherImagemButton = findViewById(R.id.escolherImagemButton)

        escolherImagemButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            startActivityForResult(intent, PICK_IMAGE)
        }

        registerButton.setOnClickListener {
            createAccount()
        }

//        sairButton.setOnClickListener {
//            finish()
//        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            imageUri = data?.data

            imagemOng.setImageURI(imageUri)
        }
    }

    private fun createAccount() {

        val name = registerOngNameEditText.text.toString().trim()
        val email = registerOngEmailEditText.text.toString().trim()
        val telefone = registerOngTelefoneEditText.text.toString().trim()
        val cnpj = registerCNPJEditText.text.toString().trim()
        val password = registerOngPasswordEditText.text.toString().trim()
        val confirmPassword = registerConfirmPasswordEditText.text.toString().trim()

        if (
            name.isEmpty() ||
            email.isEmpty() ||
            telefone.isEmpty() ||
            cnpj.isEmpty() ||
            password.isEmpty() ||
            confirmPassword.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Preencha todos os campos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (password.length < 6) {

            Toast.makeText(
                this,
                "A senha deve ter no mínimo 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (password != confirmPassword) {

            Toast.makeText(
                this,
                "As senhas não coincidem",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (imageUri == null) {

            Toast.makeText(
                this,
                "Escolha uma imagem",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val imagemBase64 = uriToBase64(imageUri!!)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = auth.currentUser
                        ?: return@addOnCompleteListener

                    val uid = user.uid

                    val ong = Ong(
                        key = uid,
                        nome_ong = name,
                        email_ong = email,
                        telefone_ong = telefone,
                        cnpj = cnpj,
                        imagemBase64 = imagemBase64
                    )

                    database
                        .child("ongs")
                        .child(uid)
                        .setValue(ong)
                        .addOnSuccessListener {

                            updateProfile(user, name)

                            sendEmailVerification(user)

                            Toast.makeText(
                                this,
                                "ONG cadastrada com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()
                        }
                        .addOnFailureListener {

                            Toast.makeText(
                                this,
                                "Erro ao salvar ONG",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } else {

                    val errorMessage =
                        task.exception?.message ?: "Erro desconhecido"

                    Log.e(
                        "FirebaseAuth",
                        "Erro ao cadastrar ONG: $errorMessage"
                    )

                    Toast.makeText(
                        this,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser?) {

        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Email de verificação enviado",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        this,
                        "Erro ao enviar email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateProfile(
        user: FirebaseUser?,
        displayName: String
    ) {

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user?.updateProfile(profileUpdates)
    }

    private fun uriToBase64(uri: Uri): String {

        val inputStream = contentResolver.openInputStream(uri)

        val bytes = inputStream?.readBytes()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}