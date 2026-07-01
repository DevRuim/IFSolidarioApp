package com.ifpr.ifsolidarioapp.ui.ranking

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.internal.ViewUtils.dpToPx
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Usuario

class RankingFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RankingAdapter

    private val listaUsuarios = mutableListOf<Usuario>()

    // Pódio
    private lateinit var layoutPodio: LinearLayout
    private lateinit var imgPodio1: ShapeableImageView
    private lateinit var imgPodio2: ShapeableImageView
    private lateinit var imgPodio3: ShapeableImageView

    private lateinit var imgFoto: ShapeableImageView
    private lateinit var tvNomePodio1: TextView
    private lateinit var tvNomePodio2: TextView
    private lateinit var tvNomePodio3: TextView
    private lateinit var tvDoacoesPodio1: TextView
    private lateinit var tvDoacoesPodio2: TextView
    private lateinit var tvDoacoesPodio3: TextView

    // Tags "Você" no pódio
    private lateinit var tvVocePodio1: TextView
    private lateinit var tvVocePodio2: TextView
    private lateinit var tvVocePodio3: TextView

    // Card fixo "Sua posição"
    private lateinit var cardSuaPosicao: LinearLayout
    private lateinit var tvSuaPosicaoNome: TextView
    private lateinit var tvSuaPosicaoDoacoes: TextView
    private lateinit var tvSuaPosicaoNumero: TextView

    private val uidAtual by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.ranking_fragment, container, false)

        // RecyclerView
        recycler = view.findViewById(R.id.recyclerRanking)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = RankingAdapter(uidAtual)
        recycler.adapter = adapter

        // Pódio
        layoutPodio      = view.findViewById(R.id.layoutPodio)
        imgPodio1        = view.findViewById(R.id.imgPodio1)
        imgPodio2        = view.findViewById(R.id.imgPodio2)
        imgPodio3        = view.findViewById(R.id.imgPodio3)
        imgFoto          = view.findViewById(R.id.imgFoto)
        tvNomePodio1     = view.findViewById(R.id.tvNomePodio1)
        tvNomePodio2     = view.findViewById(R.id.tvNomePodio2)
        tvNomePodio3     = view.findViewById(R.id.tvNomePodio3)
        tvDoacoesPodio1  = view.findViewById(R.id.tvDoacoesPodio1)
        tvDoacoesPodio2  = view.findViewById(R.id.tvDoacoesPodio2)
        tvDoacoesPodio3  = view.findViewById(R.id.tvDoacoesPodio3)
        tvVocePodio1     = view.findViewById(R.id.tvVocePodio1)
        tvVocePodio2     = view.findViewById(R.id.tvVocePodio2)
        tvVocePodio3     = view.findViewById(R.id.tvVocePodio3)

        // Card fixo
        cardSuaPosicao       = view.findViewById(R.id.cardSuaPosicao)
        tvSuaPosicaoNome     = view.findViewById(R.id.tvSuaPosicaoNome)
        tvSuaPosicaoDoacoes  = view.findViewById(R.id.tvSuaPosicaoDoacoes)
        tvSuaPosicaoNumero   = view.findViewById(R.id.tvSuaPosicaoNumero)

        carregarRanking()
        return view
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun carregarRanking() {
        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    listaUsuarios.clear()

                    for (item in snapshot.children) {
                        val usuario = item.getValue(Usuario::class.java) ?: continue
                        usuario.key = item.key ?: ""
                        listaUsuarios.add(usuario)
                    }

                    listaUsuarios.sortByDescending { it.total_doacoes }

                    // Salva posição real
                    listaUsuarios.forEachIndexed { i, u -> u.posicaoRanking = i + 1 }

                    // Usuário logado
                    val usuarioLogado = listaUsuarios.find { it.key == uidAtual }

                    atualizarPodio(usuarioLogado)
                    montarLista(usuarioLogado)
                    mostrarCardSuaPosicao(usuarioLogado)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pódio — marca "Você" se o usuário logado estiver no top 3
    // ─────────────────────────────────────────────────────────────────────────

    private fun atualizarPodio(usuarioLogado: Usuario?) {
        layoutPodio.visibility =
            if (listaUsuarios.isNotEmpty()) View.VISIBLE else View.GONE

        fun bind(
            index: Int,
            img: ShapeableImageView,
            tvNome: TextView,
            tvDoacoes: TextView,
            tvVoce: TextView
        ) {
            if (index < listaUsuarios.size) {
                val u = listaUsuarios[index]
                tvNome.text    = u.nome_usuario ?: "—"
                tvDoacoes.text = "${u.total_doacoes.toInt()} doa."
                carregarFotoPodio(u.imagemBase64, img)

                // Mostra tag "Você" se for o usuário logado
                tvVoce.visibility =
                    if (u.key == uidAtual) View.VISIBLE else View.GONE
            } else {
                tvNome.text    = "—"
                tvDoacoes.text = ""
                tvVoce.visibility = View.GONE
            }
        }

        bind(0, imgPodio1, tvNomePodio1, tvDoacoesPodio1, tvVocePodio1)
        bind(1, imgPodio2, tvNomePodio2, tvDoacoesPodio2, tvVocePodio2)
        bind(2, imgPodio3, tvNomePodio3, tvDoacoesPodio3, tvVocePodio3)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lista — 4º ao 10º, mais o usuário se estiver fora desse intervalo
    // ─────────────────────────────────────────────────────────────────────────

    private fun montarLista(usuarioLogado: Usuario?) {
        val exibicao = mutableListOf<Usuario>()

        // 4º ao 10º lugar
        val limite = minOf(listaUsuarios.size, 10)
        for (i in 3 until limite) exibicao.add(listaUsuarios[i])

        // Se o usuário estiver fora do Top 10 ele é tratado
        // pelo card fixo (cardSuaPosicao) — não entra na lista
        adapter.atualizarLista(exibicao)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Card fixo "Sua posição"
    //   • Visível  → usuário está fora do Top 10 (posição > 10)
    //   • Oculto   → usuário está no Top 3 (pódio) ou no 4º–10º (lista)
    // ─────────────────────────────────────────────────────────────────────────

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    private fun mostrarCardSuaPosicao(usuarioLogado: Usuario?) {

        if (usuarioLogado == null || usuarioLogado.posicaoRanking <= 10) {

            cardSuaPosicao.visibility = View.GONE

            return
        }

        cardSuaPosicao.visibility = View.VISIBLE

        tvSuaPosicaoNome.text =
            usuarioLogado.nome_usuario ?: ""

        tvSuaPosicaoNumero.text =
            "${usuarioLogado.posicaoRanking}º"

        tvSuaPosicaoDoacoes.text =
            "Total de ${usuarioLogado.total_doacoes.toInt()} doações"

        carregarFotoUser(
            usuarioLogado.imagemBase64,
            imgFoto
        )

        // espaço extra para o card fixo
        recycler.setPadding(
            0,
            0,
            0,
            dpToPx(140)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun carregarFotoUser(
        base64: String?,
        imgView: ShapeableImageView
    ) {
        if (!base64.isNullOrEmpty()) {
            try {

                val bytes =
                    Base64.decode(base64, Base64.DEFAULT)

                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4
                }

                val bitmap =
                    BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size,
                        options
                    )

                imgView.setImageBitmap(bitmap)
                return

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        imgView.setImageResource(R.drawable.ic_usuario)
    }

    private fun carregarFotoPodio(base64: String?, imgView: ShapeableImageView) {
        if (!base64.isNullOrEmpty()) {
            try {
                val bytes  = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgView.setImageBitmap(bitmap)
                return
            } catch (_: Exception) {}
        }
        imgView.setImageResource(R.drawable.ic_usuario)
    }
}