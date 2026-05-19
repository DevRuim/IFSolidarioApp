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
import androidx.navigation.fragment.findNavController

class
HomeFragment : Fragment() {


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val containerLayout = view.findViewById<LinearLayout>(R.id.itemContainer)

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

                            val campanha = try {
                                campanhaSnapshot.getValue(Campanha::class.java)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            } ?: continue

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
                            val endereco = itemView.findViewById<TextView>(R.id.item_endereco)
                            val meta = itemView.findViewById<TextView>(R.id.item_meta)
                            val criador = itemView.findViewById<TextView>(R.id.item_criador)

                            val doarBotao = itemView.findViewById<Button>(R.id.doarButton)

                            doarBotao.setOnClickListener {

                                Toast.makeText(
                                    container.context,
                                    "ID: $campanha_id | NOME: ${campanha.nome_campanha}",
                                    Toast.LENGTH_LONG
                                ).show()

                                val bundle = Bundle().apply {
                                    putString("campanha_id", campanha_id)
                                    putString("campanha_nome", campanha.nome_campanha)
                                    putString("categoria_campanha", campanha.categoria_campanha)
                                    putString("criadorId", campanha.criadorId)
                                    putString("quantidade_atual", campanha.quantidade_atual.toString())
                                }

                                findNavController().navigate(R.id.navigation_dashboard, bundle)
                            }

                            nome.text = "Nome: ${campanha.nome_campanha}"
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
                                campanha.criador_nome.takeIf { it.isNotBlank() } ?: "Desconhecido"
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