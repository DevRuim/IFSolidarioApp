package com.ifpr.ifsolidarioapp.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        _binding = FragmentPerfilUsuarioBinding.inflate(inflater, container, false)

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid
        if(uid ==null) {
            startActivity(Intent(context, LoginActivity::class.java))
        }
        usersReference = FirebaseDatabase.getInstance().getReference("users")

        carregaDadosDoUsuarioLogado()

        return binding.root
    }

    private fun carregaDadosDoUsuarioLogado() {
        val user = auth.currentUser

        if (user != null) {

            // Configura UI
            binding.sairButton.visibility = View.VISIBLE
            binding.registerPasswordEditText.visibility = View.GONE
            binding.registerConfirmPasswordEditText.visibility = View.GONE
            binding.registerEmailEditText.isEnabled = false

            // Preenche dados do Firebase Auth
            binding.registerNameEditText.setText(user.displayName ?: "")
            binding.registerEmailEditText.setText(user.email ?: "")

            contarDoacoesUsuario(user.uid)

            // Carrega foto com segurança
            if (user.photoUrl != null) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .into(binding.userProfileImageView)
            }

            // Carrega dados do Realtime Database
            recuperarDadosUsuario(user.uid)


            binding.salvarButton.setOnClickListener {
                updateUser()
            }

            binding.sairButton.setOnClickListener {
                signOut()
            }
        }
    }


    private fun signOut() {

        auth.signOut()

        Toast.makeText(
            context,
            "Logout realizado com sucesso!",
            Toast.LENGTH_SHORT
        ).show()

        requireActivity().finish()
    }
    private fun contarDoacoesUsuario(uid: String) {

        val ref = FirebaseDatabase.getInstance().getReference("doacoes")

        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val total = snapshot.childrenCount

                    binding.valorDoacoes.text = total.toString()
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(
                        context,
                        "Erro ao carregar doações",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun recuperarDadosUsuario(usuarioKey: String) {

        usersReference.child(usuarioKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val usuario = snapshot.getValue(Usuario::class.java)

                        usuario?.let {

                            binding.registerNameEditText.setText(it.nome ?: "")
                            binding.registerEmailEditText.setText(it.email ?: "")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Log.e("FirebaseError", error.message)

                    Toast.makeText(
                        context,
                        "Erro ao carregar dados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun updateUser() {

        val name = binding.registerNameEditText.text.toString().trim()

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

            updateProfile(user, name)

        } else {

            Toast.makeText(
                context,
                "Usuário não está logado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun updateProfile(user: FirebaseUser, displayName: String) {

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        val usuario = Usuario(
            key = user.uid,
            nome = displayName,
            email = user.email ?: ""
        )

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    saveUserToDatabase(usuario)

                } else {

                    Toast.makeText(
                        context,
                        "Erro ao atualizar perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun saveUserToDatabase(usuario: Usuario) {

        val uid = usuario.key

        if (uid.isEmpty()) {

            Toast.makeText(
                context,
                "Erro: UID inválido",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        usersReference.child(uid)
            .setValue(usuario)
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

                Log.e("FirebaseError", e.message ?: "Erro desconhecido")
            }
    }

    override fun onResume() {
        super.onResume()
        carregaDadosDoUsuarioLogado()
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}
