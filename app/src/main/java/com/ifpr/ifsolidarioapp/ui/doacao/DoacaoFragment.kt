package com.ifpr.ifsolidarioapp.ui.doacao

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.ifpr.ifsolidarioapp.MainActivity
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.DoacaoData
import com.ifpr.ifsolidarioapp.databinding.FragmentDoacaoBinding
import com.ifpr.ifsolidarioapp.ui.conquista.ConquistasManager
import java.io.ByteArrayOutputStream

class DoacaoFragment : Fragment() {

    private var _binding: FragmentDoacaoBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var campanha_id: String = ""
    private var campanha_nome: String = ""
    private var criadorId: String = ""

    private val imageList = mutableListOf<Uri>()
    private var currentImageIndex = 0
    private val MAX_FOTOS = 5

    companion object {
        private const val PICK_IMAGE_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoacaoBinding.inflate(inflater, container, false)

        auth     = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        campanha_id   = arguments?.getString("campanha_id")   ?: ""
        campanha_nome = arguments?.getString("campanha_nome") ?: ""
        criadorId     = arguments?.getString("criadorId")     ?: ""

        setupSpinner()
        setupCategoria()
        setupInfoPasso1()
        setupClicks()

        return binding.root
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navegação
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Após qualquer doação (com ou sem conquista):
     * 1. Remove o DoacaoFragment da pilha, mantendo apenas a Home.
     * 2. Pede para a MainActivity selecionar o Ranking no BottomNav.
     *
     * Resultado: pilha = [Home, Ranking]
     * → Home funciona, Perfil funciona, Doação não aparece mais ao voltar.
     */
    private fun irParaRankingAposDoacao() {
        if (_binding == null) return
        // Remove Doação (e qualquer tela intermediária) da pilha, mantém Home
        findNavController().popBackStack(R.id.navigation_home, false)
        // Sincroniza o BottomNav via MainActivity
        (requireActivity() as? MainActivity)?.selecionarRanking()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Passo 1 — textos dinâmicos por categoria
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupInfoPasso1() {
        val categoria = arguments?.getString("categoria_campanha") ?: "Roupa"
        when (categoria) {
            "Alimento" -> {
                binding.tvPerguntaCategoria.text = "Quantos alimentos você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Não perecíveis e dentro da validade):"
                binding.tvItem1.text = "• Arroz"
                binding.tvItem2.text = "• Feijão"
                binding.tvItem3.text = "• Macarrão"
                binding.tvItem4.text = "• Óleo"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens de diferentes tipos\n(Ex. 2 kg de arroz + 1 macarrão = 3 itens)"
            }
            "Brinquedo" -> {
                binding.tvPerguntaCategoria.text = "Quantos brinquedos você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Em bom estado e sem peças faltando):"
                binding.tvItem1.text = "• Bonecas"
                binding.tvItem2.text = "• Carrinhos"
                binding.tvItem3.text = "• Jogos"
                binding.tvItem4.text = "• Pelúcias"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens de diferentes tipos\n(Ex. 2 carrinhos + 1 boneca = 3 itens)"
            }
            else -> {
                binding.tvPerguntaCategoria.text = "Quantas roupas você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Qualquer tamanho e em bom estado):"
                binding.tvItem1.text = "• Camisetas"
                binding.tvItem2.text = "• Calças"
                binding.tvItem3.text = "• Cobertores"
                binding.tvItem4.text = "• Blusas"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens de diferentes tipos\n(Ex. 2 blusas + 1 coberta = 3 itens)"
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spinner
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupCategoria() {
        val cat = arguments?.getString("categoria_campanha") ?: return
        val adapter = binding.spinnerCategoria.adapter as? ArrayAdapter<String> ?: return
        val idx = adapter.getPosition(cat)
        if (idx >= 0) binding.spinnerCategoria.setSelection(idx)
        binding.spinnerCategoria.isEnabled   = false
        binding.spinnerCategoria.isClickable = false
    }

    private fun setupSpinner() {
        binding.spinnerCategoria.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Alimento", "Brinquedo", "Roupa")
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Clicks
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupClicks() {
        binding.btnProximo.setOnClickListener      { avancarParaPasso2() }
        binding.tvSair.setOnClickListener          { findNavController().popBackStack() }
        binding.buttonPrev.setOnClickListener      { navegarCarrossel(-1) }
        binding.buttonNext.setOnClickListener      { navegarCarrossel(+1) }
        binding.addImageButton.setOnClickListener  { selecionarImagem() }
        binding.editImageButton.setOnClickListener { selecionarImagem() }
        binding.btnRemoverFoto.setOnClickListener  { removerFotoAtual() }
        binding.buttonFinalizar.setOnClickListener { finalizarDoacao() }
        binding.tvVoltar.setOnClickListener        { voltarParaPasso1() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Passos
    // ─────────────────────────────────────────────────────────────────────────

    fun avancarParaPasso2() {
        if (binding.editQuantidade.text.toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Digite uma quantidade", Toast.LENGTH_SHORT).show()
            return
        }
        binding.layoutPasso1.visibility = View.GONE
        binding.layoutPasso2.visibility = View.VISIBLE
        atualizarCarrossel()
    }

    private fun voltarParaPasso1() {
        binding.layoutPasso2.visibility = View.GONE
        binding.layoutPasso1.visibility = View.VISIBLE
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carrossel
    // ─────────────────────────────────────────────────────────────────────────

    private fun navegarCarrossel(delta: Int) {
        if (imageList.isEmpty()) return
        currentImageIndex = (currentImageIndex + delta + imageList.size) % imageList.size
        atualizarCarrossel()
    }

    private fun atualizarCarrossel() {
        val temFotos = imageList.isNotEmpty()

        binding.layoutSemFotos.visibility = if (temFotos) View.GONE   else View.VISIBLE
        binding.tvPosicaoFoto.visibility  = if (temFotos) View.VISIBLE else View.GONE
        binding.btnRemoverFoto.visibility = if (temFotos) View.VISIBLE else View.GONE

        if (temFotos) {
            if (currentImageIndex >= imageList.size) currentImageIndex = imageList.size - 1
            binding.imagePreview.setImageURI(imageList[currentImageIndex])
            binding.tvPosicaoFoto.text = "${currentImageIndex + 1}/${imageList.size}"
        } else {
            binding.imagePreview.setImageDrawable(null)
        }

        val mostrarSetas = imageList.size > 1
        binding.buttonPrev.visibility = if (mostrarSetas) View.VISIBLE else View.INVISIBLE
        binding.buttonNext.visibility = if (mostrarSetas) View.VISIBLE else View.INVISIBLE

        binding.tvContadorFotos.text = "${imageList.size} / $MAX_FOTOS"
        atualizarDots()
        atualizarThumbnails()
        binding.scrollThumbnails.visibility = if (imageList.size > 1) View.VISIBLE else View.GONE
    }

    private fun atualizarDots() {
        binding.layoutDots.removeAllViews()
        if (imageList.size <= 1) return
        val dp6 = dpToPx(6); val dp4 = dpToPx(4); val dp8 = dpToPx(8)
        imageList.forEachIndexed { i, _ ->
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    if (i == currentImageIndex) dp8 else dp6,
                    if (i == currentImageIndex) dp8 else dp6
                ).also { it.setMargins(dp4, 0, dp4, 0) }
                background = ContextCompat.getDrawable(
                    requireContext(),
                    if (i == currentImageIndex) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive
                )
            }
            binding.layoutDots.addView(dot)
        }
    }

    private fun atualizarThumbnails() {
        binding.layoutThumbnails.removeAllViews()
        val dp56 = dpToPx(56); val dp2 = dpToPx(2)
        imageList.forEachIndexed { i, uri ->
            val thumb = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dp56, dp56)
                    .also { it.setMargins(dp2, 0, dp2, 0) }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
                background = ContextCompat.getDrawable(
                    requireContext(),
                    if (i == currentImageIndex) R.drawable.bg_thumb_selected
                    else R.drawable.bg_thumb_normal
                )
                setOnClickListener { currentImageIndex = i; atualizarCarrossel() }
            }
            binding.layoutThumbnails.addView(thumb)
        }
    }

    private fun removerFotoAtual() {
        if (imageList.isEmpty()) return
        imageList.removeAt(currentImageIndex)
        if (currentImageIndex >= imageList.size && currentImageIndex > 0) currentImageIndex--
        atualizarCarrossel()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Imagem
    // ─────────────────────────────────────────────────────────────────────────

    private fun selecionarImagem() {
        if (imageList.size >= MAX_FOTOS) {
            Toast.makeText(requireContext(), "Limite de $MAX_FOTOS fotos atingido", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    @Deprecated("Legado")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageList.add(uri)
                currentImageIndex = imageList.size - 1
                atualizarCarrossel()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Finalizar doação
    // ─────────────────────────────────────────────────────────────────────────

    private fun finalizarDoacao() {
        val qtdTexto = binding.editQuantidade.text.toString()
        if (qtdTexto.isEmpty()) {
            Toast.makeText(requireContext(), "Digite uma quantidade", Toast.LENGTH_SHORT).show()
            return
        }
        val quantidade    = qtdTexto.toDouble()
        val categoria     = binding.spinnerCategoria.selectedItem?.toString()
            ?: arguments?.getString("categoria_campanha") ?: "Roupa"
        val imagensBase64 = imageList.map { uriToBase64(it) }

        salvarNoBanco(DoacaoData(
            imagens       = imagensBase64,
            quantidade    = quantidade,
            categoria     = categoria,
            campanha_id   = campanha_id,
            campanha_nome = campanha_nome,
            dataDoacao    = System.currentTimeMillis()
        ))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Firebase
    // ─────────────────────────────────────────────────────────────────────────

    private fun salvarNoBanco(item: DoacaoData) {
        val uid = auth.currentUser?.uid ?: return

        database.child("doacoes").child(uid).push().setValue(item)
            .addOnSuccessListener {
                atualizarEstatisticasUsuario(uid, item.categoria, item.quantidade) { _, _ ->
                    atualizarQuantidadeCampanha(item.quantidade) {

                        // Navega para o Ranking imediatamente, sem esperar conquistas.
                        // Os lotties aparecem por cima do Ranking se alguma conquista
                        // for desbloqueada (ConquistasManager abre a ConquistaActivity).
                        irParaRankingAposDoacao()

                        // Verifica conquistas em background — não bloqueia a navegação
                        database.child("usuarios").child(uid).get()
                            .addOnSuccessListener { snap ->
                                ConquistasManager.verificarConquistas(
                                    requireContext(),
                                    uid,
                                    snap.child("alimentos").getValue(Int::class.java)            ?: 0,
                                    snap.child("roupas").getValue(Int::class.java)               ?: 0,
                                    snap.child("brinquedos").getValue(Int::class.java)           ?: 0,
                                    snap.child("total_doacoes").getValue(Double::class.java)?.toInt() ?: 0
                                )
                            }
                    }
                }
            }
    }

    private fun atualizarEstatisticasUsuario(
        uid: String, categoria: String, quantidade: Double,
        onFinish: (Int?, Int) -> Unit
    ) {
        val ref = database.child("usuarios").child(uid)
        ref.child("total_doacoes").get().addOnSuccessListener { snap ->

            val novoTotal =
                (snap.getValue(Double::class.java) ?: 0.0) + quantidade

            ref.child("total_doacoes")
                .setValue(novoTotal)
                .addOnSuccessListener {

                    atualizarTotalInterface(quantidade)

                    atualizarTotalCategoria(
                        ref,
                        categoria,
                        quantidade,
                        onFinish
                    )
                }
        }
    }

    private fun atualizarTotalInterface(
        quantidade: Double
    ) {

        val ref = database
            .child("estatisticas")
            .child("interface")
            .child("total_doacoes")

        ref.runTransaction(object : Transaction.Handler {

            override fun doTransaction(
                currentData: MutableData
            ): Transaction.Result {

                val totalAtual =
                    currentData.getValue(Double::class.java) ?: 0.0

                currentData.value =
                    totalAtual + quantidade

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
            }
        })
    }
    private fun atualizarTotalCategoria(
        ref: DatabaseReference, categoria: String, quantidade: Double,
        onFinish: (Int?, Int) -> Unit
    ) {
        val campo = when (categoria) {
            "Alimento"  -> "alimentos"
            "Brinquedo" -> "brinquedos"
            "Roupa"     -> "roupas"
            else        -> { onFinish(null, 0); return }
        }
        ref.child(campo).get().addOnSuccessListener { snap ->
            val novo = (snap.getValue(Int::class.java) ?: 0) + quantidade.toInt()
            ref.child(campo).setValue(novo).addOnSuccessListener { onFinish(null, novo) }
        }
    }

    private fun atualizarQuantidadeCampanha(quantidade: Double, onFinish: () -> Unit) {
        if (campanha_id.isEmpty() || criadorId.isEmpty()) { onFinish(); return }
        val ref = database.child("campanhas").child(criadorId).child(campanha_id)
        ref.child("quantidade_atual").get().addOnSuccessListener { snap ->
            val nova = (snap.getValue(Double::class.java) ?: 0.0) + quantidade
            ref.child("quantidade_atual").setValue(nova).addOnSuccessListener { onFinish() }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitários
    // ─────────────────────────────────────────────────────────────────────────

    private fun uriToBase64(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val original    = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val resized = Bitmap.createScaledBitmap(
            original,
            800,
            (original.height * 800) / original.width,
            true
        )

        val out = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70, out)
        original.recycle()
        resized.recycle()

        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}