package com.ifpr.ifsolidarioapp.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.ifpr.ifsolidarioapp.baseclasses.Usuario
import com.ifpr.ifsolidarioapp.databinding.FragmentPerfilUsuarioBinding
import com.ifpr.ifsolidarioapp.ui.login.LoginActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import android.util.Base64

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPerfilUsuarioBinding.inflate(
            inflater,
            container,
            false
        )

        val view = binding.root

        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid

        if (uid == null) {

            startActivity(Intent(context, LoginActivity::class.java))
            requireActivity().finish()

            return view
        }

        usersReference =
            FirebaseDatabase.getInstance()
                .getReference("usuarios")

        binding.buttonEditarPerfil.setOnClickListener {

            // ação aqui
        }

        binding.buttonSair.setOnClickListener {

            signOut()
        }

        carregarDadosUsuario()

        return view
    }

    private fun carregarDadosUsuario() {

        val uid = auth.currentUser?.uid ?: return

        val user = FirebaseAuth.getInstance().currentUser

        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    val imagemBase64 =
                        snapshot.child("imagemBase64")
                            .getValue(String::class.java)

                    if (!imagemBase64.isNullOrEmpty()) {

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

                            binding.imageViewFoto.setImageBitmap(bitmap)

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }

                    val nome = snapshot.child("nome_usuario").getValue(String::class.java)
                    val email = snapshot.child("email_usuario").getValue(String::class.java)
                    val telefone = snapshot.child("telefone_usuario").getValue(String::class.java)
                    val alimentos = snapshot.child("alimentos").getValue(Int::class.java) ?: 0
                    val brinquedos = snapshot.child("brinquedos").getValue(Int::class.java) ?: 0
                    val roupas = snapshot.child("roupas").getValue(Int::class.java) ?: 0
                    val total = snapshot.child("total_doacoes").getValue(Int::class.java) ?: 0
                    val conquistas = snapshot.child("conquistas").getValue(Int::class.java) ?: 0
                    val tipo = snapshot.child("tipo_usuario").getValue(String::class.java)

                    binding.textViewNameUsuario.text = nome
                    binding.textViewEmail.text = email
                    binding.textViewTelefone.text = telefone
                    binding.textViewTotal.text = total.toString()
                    binding.textViewAlimentos.text = alimentos.toString()
                    binding.textViewBrinquedos.text = brinquedos.toString()
                    binding.textViewRoupas.text = roupas.toString()
                    binding.textViewConquistas.text = conquistas.toString()

                }
            }
    }


    private fun signOut() {

        auth.signOut()

        startActivity(
            Intent(context, LoginActivity::class.java)
        )

        requireActivity().finish()
    }

    private fun updateUser() {

        val name = "" //binding.editTextNome.text.toString().trim()
        val telefone = ""//binding.editTextTelefone.text.toString().trim()

        if (name.isEmpty()) {

            Toast.makeText(
                context,
                "Digite um nome válido",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val user = auth.currentUser

        if (user != null) {

            updateProfile(
                user,
                name,
                telefone
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
        telefone: String
    ) {

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    saveUserToDatabase(
                        displayName,
                        telefone
                    )

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

        val updates = mapOf(
            "nome_usuario" to nome,
            "telefone_usuario" to telefone
        )

        usersReference.child(uid)
            .updateChildren(updates)
            .addOnSuccessListener {

                Toast.makeText(
                    context,
                    "Usuário atualizado com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->

                Toast.makeText(
                    context,
                    "Erro ao salvar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        carregarDadosUsuario()
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}
