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
import com.ifpr.ifsolidarioapp.baseclasses.DoacaoData
import android.content.Intent
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.ifsolidarioapp.ui.login.LoginActivity
import com.ifpr.ifsolidarioapp.ui.usuario.CadastroUsuarioActivity
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.concurrent.TimeUnit
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView

data class CampanhaItem(
    val campanha: Campanha,
    val campanhaId: String
)

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

        val scrollView =
            view.findViewById<ScrollView>(R.id.scrollView)

        val totalContainer =
            view.findViewById<FrameLayout>(R.id.totalDoacoesContainer)

        scrollView.viewTreeObserver.addOnScrollChangedListener {

            val scrollY = scrollView.scrollY

            // fade progressivo
            val alpha = 1f - (scrollY / 300f)

            totalContainer.alpha =
                alpha.coerceIn(0f, 1f)
        }


        auth = FirebaseAuth.getInstance()

        contarTotalDoacoes(view)

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

                    val listaCampanhas = mutableListOf<Pair<Campanha, String>>()

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

                            listaCampanhas.add(Pair(campanha, campanha_id))

                        }
                    }

                    val formato =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                    val hoje = Date()

                    val campanhasOrdenadas =
                        listaCampanhas
                            .filter {

                                try {

                                    val dataFinal =
                                        formato.parse(it.first.data_termino)

                                    dataFinal.time >= hoje.time - 86400000

                                } catch (e: Exception) {

                                    false
                                }
                            }
                            .sortedBy {

                                try {

                                    formato.parse(it.first.data_termino)

                                } catch (e: Exception) {

                                    null
                                }
                            }

                    for ((campanha, campanha_id) in campanhasOrdenadas) {

                        val itemView = LayoutInflater.from(container.context)
                            .inflate(R.layout.item_template, container, false)

                        val img = itemView.findViewById<ImageView>(R.id.item_image)

                        val nome = itemView.findViewById<TextView>(R.id.item_nome)

                        val desc = itemView.findViewById<TextView>(R.id.item_descricao)

                        val categoria =
                            itemView.findViewById<TextView>(R.id.item_categoria)

                        val data =
                            itemView.findViewById<TextView>(R.id.item_data)

                        val instagram = campanha.instagram

                        val progresso =
                            itemView.findViewById<TextView>(R.id.item_progresso)

                        val progressBar =
                            itemView.findViewById<ProgressBar>(R.id.progressBarCampanha)

                        val porcentagem =
                            ((campanha.quantidade_atual / campanha.meta) * 100).toInt()

                        progressBar.progress = porcentagem

                        val doarBotao =
                            itemView.findViewById<Button>(R.id.doarButton)

                        val instagramBotao =
                            itemView.findViewById<ImageButton>(R.id.btnInstagram)

                        nome.text = campanha.nome_campanha

                        desc.text = "     ${campanha.descricao}"

                        categoria.text =
                            "Categoria: ${campanha.categoria_campanha}"

                        val unidade =
                            obterUnidade(campanha.categoria_campanha)

                        progresso.text =
                            "${campanha.quantidade_atual.toInt()} de ${campanha.meta.toInt()} $unidade"

                        data.text =
                            obterTextoData(campanha.data_termino)

                        val textoData =
                            obterTextoData(campanha.data_termino)

                        if (
                            textoData.contains("hoje") ||
                            textoData.contains("amanhã") ||
                            textoData.contains("2 dias") ||
                            textoData.contains("3 dias")
                        ) {

                            // ALERTA VERMELHO

                            data.setTextColor(
                                resources.getColor(android.R.color.white)
                            )

                            data.setBackgroundResource(
                                R.drawable.bg_alerta_vermelho
                            )

                            data.setPadding(24, 10, 24, 10)

                        } else {

                            // VOLTA AO NORMAL

                            data.setTextColor(
                                resources.getColor(R.color.black)
                            )

                            data.background = null

                            data.setPadding(0, 0, 0, 0)
                        }

                        try {

                            val bytes = Base64.decode(
                                campanha.imagemBase64,
                                Base64.DEFAULT
                            )

                            val bitmap = BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size
                            )

                            img.setImageBitmap(bitmap)

                        } catch (e: Exception) {

                            img.setImageResource(
                                android.R.drawable.ic_menu_report_image
                            )
                        }

                        doarBotao.setOnClickListener {

                            val uid = auth.currentUser?.uid

                            if (uid != null) {

                                val bundle = Bundle().apply {

                                    putString("campanha_id", campanha_id)

                                    putString(
                                        "campanha_nome",
                                        campanha.nome_campanha
                                    )

                                    putString(
                                        "categoria_campanha",
                                        campanha.categoria_campanha
                                    )

                                    putString(
                                        "criadorId",
                                        campanha.criadorId
                                    )

                                    putString(
                                        "quantidade_atual",
                                        campanha.quantidade_atual.toString()
                                    )
                                }

                                findNavController().navigate(
                                    R.id.navigation_dashboard,
                                    bundle
                                )

                            } else {

                                startActivity(
                                    Intent(context, LoginActivity::class.java)
                                )
                            }
                        }

                        instagramBotao.setOnClickListener {

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                android.net.Uri.parse(
                                    "https://instagram.com/$instagram"
                                )
                            )

                            startActivity(intent)
                        }

                        // ANIMAÇÃO DOS CARDS

                        itemView.alpha = 0f
                        itemView.translationY = 50f

                        itemView.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(500)
                            .setStartDelay((container.childCount * 90).toLong())
                            .start()

                        container.addView(itemView)
                    }
                }

                private fun obterUnidade(categoria: String): String {

                    return when (categoria.lowercase()) {

                        "alimento", "alimentos" -> "Kg"

                        "brinquedo", "brinquedos" -> "un."

                        "roupa", "roupas" -> "peças"

                        else -> "un."
                    }
                }
                @SuppressLint("SetTextI18n")
                private fun obterTextoData(dataTermino: String): String {

                    return try {

                        val formato =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        val dataFinal =
                            formato.parse(dataTermino)

                        val hoje = formato.parse(formato.format(Date()))

                        val diferencaMillis =
                            dataFinal.time - hoje.time

                        val dias =
                            TimeUnit.MILLISECONDS.toDays(diferencaMillis).toInt()

                        when {

                            dias > 3 ->
                                "Termina em: $dataTermino"

                            dias == 3 ->
                                "Termina em 3 dias"

                            dias == 2 ->
                                "Termina em 2 dias"

                            dias == 1 ->
                                "Termina amanhã"

                            dias == 0 ->
                                "Termina hoje"

                            else ->
                                "Campanha encerrada"
                        }

                    } catch (e: Exception) {

                        "Data inválida"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(container.context, "Erro ao carregar", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun contarTotalDoacoes(view: View) {

        val totalText =
            view.findViewById<TextView>(R.id.textTotalDoacoes)

        val loading =
            view.findViewById<com.airbnb.lottie.LottieAnimationView>(
                R.id.loadingTotalDoacoes
            )
        val particles =
            view.findViewById<LottieAnimationView>(
                R.id.particlesAnimation
            )

        val ref = FirebaseDatabase.getInstance()
            .getReference("doacoes")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var totalQuantidade = 0.0

                for (usuarioSnapshot in snapshot.children) {
                    for (doacaoSnapshot in usuarioSnapshot.children) {
                        val valor =
                            doacaoSnapshot.child("quantidade").value

                        when (valor) {
                            is Long -> {
                                totalQuantidade += valor.toDouble()
                            }

                            is Double -> {
                                totalQuantidade += valor
                            }

                            is Int -> {
                                totalQuantidade += valor.toDouble()
                            }
                        }
                    }
                }

                loading.visibility = View.GONE

                particles.visibility = View.VISIBLE
                particles.playAnimation()

                totalText.visibility = View.VISIBLE

                totalText.text =
                    totalQuantidade.toInt().toString()

                // Animação de fade

                totalText.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start()

                totalText.scaleX = 0.8f
                totalText.scaleY = 0.8f
                totalText.alpha = 0f

                totalText.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .start()
            }

            override fun onCancelled(error: DatabaseError) {

                loading.visibility = View.GONE

                totalText.visibility = View.VISIBLE

                totalText.text = "--"

                Toast.makeText(
                    context,
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}