package com.ifpr.ifsolidarioapp.ui.home

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Campanha
import android.content.Intent
import com.ifpr.ifsolidarioapp.ui.campanha.CadastroCampanhaActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.ifsolidarioapp.ui.login.LoginActivity
import com.ifpr.ifsolidarioapp.ui.usuario.CadastroUsuarioActivity

class
HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val containerLayout = view.findViewById<LinearLayout>(R.id.itemContainer)
        val botao = view.findViewById<Button>(R.id.abrir_campanha)

        auth = FirebaseAuth.getInstance()

        botao.setOnClickListener {
            val intent = Intent(requireContext(), CadastroCampanhaActivity::class.java)
            startActivity(intent)
        }

        carregarCampanhas(containerLayout)

        return view
    }

    private fun carregarCampanhas(container: LinearLayout) {

        val db = FirebaseDatabase.getInstance().reference

        db.child("campanhas")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {

                    container.removeAllViews()

                    for (userSnapshot in snapshot.children) {
                        for (campanhaSnapshot in userSnapshot.children) {

                            val campanha = campanhaSnapshot.getValue(Campanha::class.java)
                                ?: continue

                            val itemView = LayoutInflater.from(container.context)
                                .inflate(R.layout.item_template, container, false)

                            val img = itemView.findViewById<ImageView>(R.id.item_image)
                            val nome = itemView.findViewById<TextView>(R.id.item_nome)
                            val desc = itemView.findViewById<TextView>(R.id.item_descricao)
                            val endereco = itemView.findViewById<TextView>(R.id.item_endereco)
                            val meta = itemView.findViewById<TextView>(R.id.item_meta)
                            val criador = itemView.findViewById<TextView>(R.id.item_criador)

                            val doarBotao = itemView.findViewById<Button>(R.id.doarButton)

                            doarBotao.setOnClickListener {
                                val uid = auth.currentUser?.uid
                                if(uid !=null)
                                findNavController().navigate(R.id.navigation_dashboard)
                                else{
                                    startActivity(Intent(context, LoginActivity::class.java))
                                }


                            }

                            nome.text = "Nome: ${campanha.nomeCampanha}"
                            desc.text = "Descrição: ${campanha.descricao}"
                            endereco.text = "Endereço: ${campanha.endereco}"
                            meta.text = "Meta: R$ ${campanha.meta}"

                            try {
                                val bytes = Base64.decode(campanha.imagemBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                img.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                img.setImageResource(android.R.drawable.ic_menu_report_image)
                            }

                            criador.text = "Criado por: ${
                                campanha.criadorNome.takeIf { it.isNotBlank() } ?: "Desconhecido"
                            }"

                            container.addView(itemView)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(container.context, "Erro ao carregar", Toast.LENGTH_SHORT).show()
                }
            })
    }
}