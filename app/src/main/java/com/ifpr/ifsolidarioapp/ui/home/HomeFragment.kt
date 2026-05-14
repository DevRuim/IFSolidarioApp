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
import android.widget.Button

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

        auth = FirebaseAuth.getInstance()

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

                            val campanha_id = campanhaSnapshot.key

                            if (campanha_id.isNullOrEmpty()) {
                                Toast.makeText(container.context, "Erro: ID nulo", Toast.LENGTH_SHORT).show()
                                continue
                            }

                            val itemView = LayoutInflater.from(container.context)
                                .inflate(R.layout.item_template, container, false)

                            val img = itemView.findViewById<ImageView>(R.id.item_image)
                            val nome = itemView.findViewById<TextView>(R.id.item_nome)
                            val desc = itemView.findViewById<TextView>(R.id.item_descricao)
                            val categoria = itemView.findViewById<TextView>(R.id.item_categoria)
                            //val endereco = itemView.findViewById<TextView>(R.id.item_endereco)
                            //val criador = itemView.findViewById<TextView>(R.id.item_criador)
                            val data = itemView.findViewById<TextView>(R.id.item_data)
                            val progresso = itemView.findViewById<TextView>(R.id.item_progresso)

                            val doarBotao = itemView.findViewById<Button>(R.id.doarButton)

                            doarBotao.setOnClickListener {
                                val uid = auth.currentUser?.uid
                                if(uid !=null) {
                                    Toast.makeText(
                                        container.context,
                                        "ID: $campanha_id | NOME: ${campanha.nome_campanha}",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val bundle = Bundle().apply {
                                        putString("campanha_id", campanha_id)
                                        putString("campanha_nome", campanha.nome_campanha)
                                        putString("categoria_campanha", campanha.categoria_campanha)
                                    }

                                    findNavController().navigate(R.id.navigation_dashboard, bundle)
                                } else{
                                    startActivity(Intent(context, LoginActivity::class.java))
                                }
                            }

                            nome.text = campanha.nome_campanha

                            desc.text = campanha.descricao

                            categoria.text = "Categoria: ${campanha.categoria_campanha}"

                            val unidade = obterUnidade(campanha.categoria_campanha)

                            progresso.text =
                                "${campanha.quantidade_atual.toInt()} de ${campanha.meta.toInt()} $unidade"

                            data.text = "Termina em: ${campanha.data_termino}"

                            try {
                                val bytes = Base64.decode(campanha.imagemBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                img.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                img.setImageResource(android.R.drawable.ic_menu_report_image)
                            }

                            /*criador.text = "Criado por: ${
                                campanha.criador_nome.takeIf { it.isNotBlank() } ?: "Desconhecido"
                            }"*/

                            container.addView(itemView)
                        }
                    }
                }

                private fun obterUnidade(categoria: String): String {

                    return when (categoria.lowercase()) {

                        "alimento", "alimentos" -> "Kg"

                        "brinquedo", "brinquedos" -> "un."

                        "roupa", "roupas" -> "Pç"

                        else -> "un."
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(container.context, "Erro ao carregar", Toast.LENGTH_SHORT).show()
                }
            })
    }

}