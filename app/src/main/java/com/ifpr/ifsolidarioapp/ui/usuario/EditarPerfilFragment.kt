package com.ifpr.ifsolidarioapp.ui.usuario

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.databinding.FragmentEditarPerfilBinding
import com.ifpr.ifsolidarioapp.ui.login.LoginActivity
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController

class EditarPerfilFragment : Fragment() {

    private var _binding: FragmentEditarPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var tipoUsuario: String = ""
    private var imagemBase64: String = ""

    private val selecionarImagem =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {
                imagemBase64 = uriToBase64(uri)

                binding.imageViewUsuario.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditarPerfilBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid

        if (uid == null) {

            val intent = Intent(
                context,
                LoginActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            requireActivity().finish()

            return binding.root
        }
        setupClicks()
        carregarDadosUsuario()

        return binding.root
    }

    private fun carregarFoto(snapshot: DataSnapshot) {

        val imagemBase64 = snapshot
            .child("imagemBase64")
            .getValue(String::class.java)

        if (imagemBase64.isNullOrEmpty()) {

            binding.imageViewUsuario.setImageResource(
                R.drawable.ic_profile_white
            )

            return
        }

        try {

            val bytes = Base64.decode(
                imagemBase64,
                Base64.DEFAULT
            )

            val bitmap = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )

            binding.imageViewUsuario.setImageBitmap(bitmap)

        } catch (e: Exception) {

            e.printStackTrace()

            binding.imageViewUsuario.setImageResource(
                R.drawable.ic_profile_white
            )
        }
    }

    private fun carregarDadosUsuario() {

        val uid = auth.currentUser?.uid ?: return

        val usuariosRef =
            FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)

        val ongsRef =
            FirebaseDatabase.getInstance()
                .getReference("ongs")
                .child(uid)

        usuariosRef.get().addOnSuccessListener { usuarioSnapshot ->

            if (usuarioSnapshot.exists()) {

                binding.textViewOng.visibility = View.GONE
                binding.textViewCNPJ.visibility = View.GONE
                binding.editTextCNPJ.visibility = View.GONE

                binding.textViewUsuario.visibility = View.VISIBLE

                tipoUsuario = "DOADOR"

                carregarDadosDoador(usuarioSnapshot)

            } else {

                ongsRef.get()
                    .addOnSuccessListener { ongSnapshot ->

                        if (ongSnapshot.exists()) {

                            binding.textViewUsuario.visibility = View.GONE

                            binding.textViewOng.visibility = View.VISIBLE
                            binding.textViewCNPJ.visibility = View.VISIBLE
                            binding.editTextCNPJ.visibility = View.VISIBLE

                            tipoUsuario = "ONG"

                            carregarDadosOng(ongSnapshot)
                        }
                    }
            }
        }
    }

    private fun carregarDadosDoador(
        snapshot: DataSnapshot
    ) {

        carregarFoto(snapshot)

        val nome = snapshot.child("nome_usuario")
            .getValue(String::class.java) ?: ""

        val telefone = snapshot.child("telefone_usuario")
            .getValue(String::class.java) ?: ""

        binding.editTextName.setText(nome)
        binding.editTextTelefone.setText(telefone)
    }

    private fun carregarDadosOng(
        snapshot: DataSnapshot
    ) {

        carregarFoto(snapshot)

        val nome = snapshot.child("nome_ong")
            .getValue(String::class.java) ?: ""

        val telefone = snapshot.child("telefone_ong")
            .getValue(String::class.java) ?: ""

        val cnpj = snapshot.child("cnpj")
            .getValue(String::class.java) ?: ""

        binding.editTextName.setText(nome)
        binding.editTextTelefone.setText(telefone)
        binding.editTextCNPJ.setText(cnpj)
    }


    private fun updateUser() {
        val name = binding.editTextName.text.toString().trim()
        val telefone = binding.editTextTelefone.text.toString().trim()
        val cnpj = binding.editTextCNPJ.text.toString().trim()

        if (name.isEmpty()) {

            Toast.makeText(
                context,
                "Digite um nome válido",
                Toast.LENGTH_SHORT
            ).show()

            return
        }
        if (telefone.isEmpty()) {

            Toast.makeText(
                context,
                "Digite um número de telefone",
                Toast.LENGTH_SHORT
            ).show()

            return
        }
        if(tipoUsuario == "ONG"){
            if (cnpj.isEmpty()) {

                Toast.makeText(
                    context,
                    "Digite o cnpj",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }
        }

        val user = auth.currentUser

        if (user != null) {
            updateProfile(
                user,
                name,
                telefone,
                cnpj
            )

        } else {

            Toast.makeText(
                context,
                "Usuário não está logado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateProfile(
        user: FirebaseUser,
        displayName: String,
        telefone: String,
        cnpj: String
    ) {

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    when (tipoUsuario) {

                        "ONG" -> {
                            saveOngToDatabase(
                                displayName,
                                telefone,
                                cnpj
                            )
                        }

                        "DOADOR" -> {
                            saveUserToDatabase(
                                displayName,
                                telefone
                            )
                        }

                        else -> {
                            Toast.makeText(
                                context,
                                "Tipo de usuário inválido",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {

                    Toast.makeText(
                        context,
                        "Erro ao atualizar perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(
        nome: String,
        telefone: String
    ) {

        val uid = auth.currentUser?.uid ?: return


        val updates = mutableMapOf<String, Any>(
            "nome_usuario" to nome,
            "telefone_usuario" to telefone
        )

        if (imagemBase64.isNotEmpty()) {
            updates["imagemBase64"] = imagemBase64
        }

        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .updateChildren(updates)

            .addOnSuccessListener {

                Toast.makeText(
                    context,
                    "Usuário atualizado com sucesso",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().popBackStack()
            }

            .addOnFailureListener { e ->

                Toast.makeText(
                    context,
                    "Erro ao salvar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                findNavController().popBackStack()
            }
    }

    private fun saveOngToDatabase(
        nome: String,
        telefone: String,
        cnpj: String
    ) {

        val uid = auth.currentUser?.uid ?: return

        val updates = mutableMapOf<String, Any>(
            "nome_ong" to nome,
            "telefone_ong" to telefone,
            "cnpj" to cnpj
        )

        if (imagemBase64.isNotEmpty()) {
            updates["imagemBase64"] = imagemBase64
        }

        FirebaseDatabase.getInstance()
            .getReference("ongs")
            .child(uid)
            .updateChildren(updates)

            .addOnSuccessListener {

                Toast.makeText(
                    context,
                    "Usuário atualizado com sucesso",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().popBackStack()
            }

            .addOnFailureListener { e ->

                Toast.makeText(
                    context,
                    "Erro ao salvar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                findNavController().popBackStack()
            }
    }

    private fun setupClicks(){
        binding.buttonSalvar.setOnClickListener {
            updateUser()
        }
        binding.imageViewUsuario.setOnClickListener {

            selecionarImagem.launch("image/*")
        }
    }

    private fun uriToBase64(uri: Uri): String {

        val ctx = context ?: return ""

        val inputStream =
            ctx.contentResolver
                .openInputStream(uri)
                ?: return ""

        val bytes = inputStream.use {
            it.readBytes()
        }

        return Base64.encodeToString(
            bytes,
            Base64.DEFAULT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}