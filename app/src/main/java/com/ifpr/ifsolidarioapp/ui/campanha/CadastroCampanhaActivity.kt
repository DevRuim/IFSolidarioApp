package com.ifpr.ifsolidarioapp.ui.campanha

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Campanha

class CadastroCampanhaActivity : AppCompatActivity() {

    private lateinit var textCadastroCampanhaTitle: TextView
    private lateinit var registerNameCampanhaEditText: EditText
    private lateinit var registerEnderecoEditText: EditText
    private lateinit var registerDescricaoEditText: EditText
    private lateinit var registerMetaEditText: EditText
    private lateinit var registerCampanhaButton: Button
    private lateinit var sairButton: Button
    private lateinit var imagemCampanha: ImageView
    private lateinit var escolherImagemButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    companion object {
        const val PICK_IMAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_campanha)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        textCadastroCampanhaTitle = findViewById(R.id.textCadastroCampanhaTitle)
        registerNameCampanhaEditText = findViewById(R.id.registerNameCampanhaEditText)
        registerEnderecoEditText = findViewById(R.id.registerEnderecoEditText)
        registerDescricaoEditText = findViewById(R.id.registerDescricaoEditText)
        registerMetaEditText = findViewById(R.id.registerMetaEditText)

        imagemCampanha = findViewById(R.id.imagemCampanha)
        escolherImagemButton = findViewById(R.id.buttonEscolherImagem)

        registerCampanhaButton = findViewById(R.id.registerCampanhaButton)
        sairButton = findViewById(R.id.sairButton)

        escolherImagemButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)

        }

        registerCampanhaButton.setOnClickListener {
            createCampanha()
        }

        sairButton.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            imageUri = data?.data
            imagemCampanha.setImageURI(imageUri)

        }
    }

    private fun createCampanha() {

        val nomeCampanha = registerNameCampanhaEditText.text.toString().trim()
        val endereco = registerEnderecoEditText.text.toString().trim()
        val descricao = registerDescricaoEditText.text.toString().trim()
        val metaTexto = registerMetaEditText.text.toString().trim()

        if (nomeCampanha.isEmpty() || endereco.isEmpty() || descricao.isEmpty() || metaTexto.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Escolha uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        val meta = metaTexto.toDouble()
        val userId = auth.currentUser?.uid ?: return

        val campanhaKey = database.child("campanhas").push().key ?: return

        val campanha = Campanha(
            key = campanhaKey,
            nomeCampanha = nomeCampanha,
            endereco = endereco,
            descricao = descricao,
            meta = meta,
            criadorId = userId,
            imagemUri = imageUri.toString()
        )

        database.child("campanhas")
            .child(userId)
            .child(campanhaKey)
            .setValue(campanha)
            .addOnSuccessListener {

                Toast.makeText(this, "Campanha criada!", Toast.LENGTH_SHORT).show()
                finish()

            }
            .addOnFailureListener {

                Toast.makeText(this, "Erro ao salvar campanha", Toast.LENGTH_SHORT).show()

            }
    }
}