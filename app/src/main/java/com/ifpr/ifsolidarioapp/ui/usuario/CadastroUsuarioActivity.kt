package com.ifpr.ifsolidarioapp.ui.usuario

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Ong
import com.ifpr.ifsolidarioapp.baseclasses.Usuario

class CadastroUsuarioActivity : AppCompatActivity() {

    private lateinit var selectUsuario: MaterialAutoCompleteTextView
    private lateinit var textViewUsuario: TextView
    private lateinit var textViewOng: TextView
    private lateinit var textViewCNPJ: TextView
    private lateinit var registerNameEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerTelefoneEditText: EditText
    private lateinit var registerCNPJEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var registerButton: Button
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
        setContentView(R.layout.activity_cadastro_usuario)

        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance().reference

        selectUsuario = findViewById(R.id.selectUsuario)

        textViewUsuario = findViewById(R.id.textViewUsuario)
        textViewOng = findViewById(R.id.textViewOng)
        textViewCNPJ = findViewById(R.id.textViewCNPJ)

        registerNameEditText = findViewById(R.id.registerNameEditText)
        registerEmailEditText = findViewById(R.id.registerEmailEditText)
        registerTelefoneEditText = findViewById(R.id.registerTelefoneEditText)
        registerCNPJEditText = findViewById(R.id.registerCNPJEditText)
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText)
        registerConfirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText)

        registerButton = findViewById(R.id.registerButton)

        imagemOng = findViewById(R.id.imagemOng)
        escolherImagemButton = findViewById(R.id.escolherImagemButton)

        escolherImagemButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)

            intent.type = "image/*"

            startActivityForResult(intent, PICK_IMAGE)
        }

        setupSelect()

        registerButton.setOnClickListener {
            createAccount()
        }
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

    private fun setupSelect() {

        val tipos = arrayOf(
            "Doador",
            "ONG"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tipos
        )

        selectUsuario.setAdapter(adapter)

        selectUsuario.inputType = 0
        selectUsuario.keyListener = null

        selectUsuario.setText(tipos[0], false)

        atualizarCampos()

        selectUsuario.setOnItemClickListener { _, _, _, _ ->
            atualizarCampos()
        }
    }

    private fun atualizarCampos() {

        val tipo = selectUsuario.text.toString()

        if (tipo == "ONG") {
            textViewUsuario.visibility = View.GONE
            textViewOng.visibility = View.VISIBLE
            textViewCNPJ.visibility = View.VISIBLE
            registerCNPJEditText.visibility = View.VISIBLE

        } else {
            textViewUsuario.visibility = View.VISIBLE
            textViewOng.visibility = View.GONE
            textViewCNPJ.visibility = View.GONE
            registerCNPJEditText.visibility = View.GONE
        }
    }

    private fun createAccount() {

        val tipoUsuario = selectUsuario.text.toString()
        val name = registerNameEditText.text.toString().trim()
        val email = registerEmailEditText.text.toString().trim()
        val telefone = registerTelefoneEditText.text.toString().trim()
        val cnpj = registerCNPJEditText.text.toString().trim()
        val password = registerPasswordEditText.text.toString().trim()
        val confirmPassword =
            registerConfirmPasswordEditText.text.toString().trim()

        if (
            name.isEmpty() ||
            email.isEmpty() ||
            telefone.isEmpty() ||
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

        if (tipoUsuario == "ONG" && cnpj.isEmpty()) {

            Toast.makeText(
                this,
                "Preencha o CNPJ",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (password.length < 6) {

            Toast.makeText(
                this,
                "Senha mínima de 6 caracteres",
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

        val imagemBase64 = if (imageUri != null) {
            uriToBase64(imageUri!!)
        } else {
            ""
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user =
                        auth.currentUser ?: return@addOnCompleteListener

                    val uid = user.uid

                    var raiz = "usuarios"
                    val usuario = if (tipoUsuario == "ONG") {

                        raiz = "ongs"
                        Ong(
                            key = uid,
                            nome_ong = name,
                            email_ong = email,
                            telefone_ong = telefone,
                            tipo_usuario = tipoUsuario,
                            cnpj = cnpj,
                            imagemBase64 = imagemBase64
                        )

                    } else {

                        Usuario(
                            key = uid,
                            nome_usuario = name,
                            email_usuario = email,
                            telefone_usuario = telefone,
                            tipo_usuario = tipoUsuario,
                            imagemBase64 = imagemBase64
                        )
                    }

                    database
                        .child(raiz)
                        .child(uid)
                        .setValue(usuario)
                        .addOnSuccessListener {

                            updateProfile(user, name)

                            sendEmailVerification(user)

                            Toast.makeText(
                                this,
                                "Usuário cadastrado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {

                            Toast.makeText(
                                this,
                                "Erro ao salvar usuário",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } else {

                    val errorMessage =
                        task.exception?.message ?: "Erro desconhecido"

                    Log.e(
                        "FirebaseAuth",
                        errorMessage
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
                        baseContext,
                        "Email de verificação enviado",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                } else {
                    Toast.makeText(
                        baseContext,
                        "Falha ao enviar email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateProfile(
        user: FirebaseUser?,
        displayName: String
    ) {

        val profileUpdates =
            UserProfileChangeRequest.Builder()
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