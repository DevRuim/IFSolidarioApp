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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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

    companion object {
        private const val PICK_IMAGE_CODE = 1000
    }

    private val conquistaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            findNavController().navigate(R.id.navigation_home)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoacaoBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        campanha_id   = arguments?.getString("campanha_id")    ?: ""
        campanha_nome = arguments?.getString("campanha_nome")  ?: ""
        criadorId     = arguments?.getString("criadorId")      ?: ""

        setupSpinner()
        setupCategoria()
        setupInfoPasso1()
        setupClicks()

        return binding.root
    }

    // ── Configura textos do Passo 1 de acordo com a categoria ────────────────

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
                    "Você pode doar vários itens\nde diferentes tipos\n(Ex. 2 kg de arroz + 1 pacote de macarrão = 3 itens)"
            }
            "Brinquedo" -> {
                binding.tvPerguntaCategoria.text = "Quantos brinquedos você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Em bom estado e sem peças faltando):"
                binding.tvItem1.text = "• Bonecas"
                binding.tvItem2.text = "• Carrinhos"
                binding.tvItem3.text = "• Jogos"
                binding.tvItem4.text = "• Pelúcias"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens\nde diferentes tipos\n(Ex. 2 carrinhos + 1 boneca = 3 itens)"
            }
            else -> { // Roupa (padrão)
                binding.tvPerguntaCategoria.text = "Quantas roupas você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Qualquer tamanho e em bom estado):"
                binding.tvItem1.text = "• Camisetas"
                binding.tvItem2.text = "• Calças"
                binding.tvItem3.text = "• Cobertores"
                binding.tvItem4.text = "• Blusas"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens\nde diferentes tipos\n(Ex. 2 blusas + 1 coberta = 3 itens )"
            }
        }
    }

    // ── Spinner de categoria (mantido para compatibilidade) ──────────────────

    private fun setupCategoria() {
        val categoriaRecebida = arguments?.getString("categoria_campanha")
        categoriaRecebida?.let { categoria ->
            val adapter = binding.spinnerCategoria.adapter as? ArrayAdapter<String>
            val index = adapter?.getPosition(categoria) ?: -1
            if (index >= 0) binding.spinnerCategoria.setSelection(index)
            binding.spinnerCategoria.isEnabled   = false
            binding.spinnerCategoria.isClickable = false
        }
    }

    private fun setupSpinner() {
        val categorias = arrayOf("Alimento", "Brinquedo", "Roupa")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categorias
        )
        binding.spinnerCategoria.adapter = adapter
    }

    // ── Clicks ───────────────────────────────────────────────────────────────

    private fun setupClicks() {

        // Passo 1 → Passo 2
        binding.tvSair.setOnClickListener {
            findNavController().popBackStack()
        }

        // Avança para Passo 2 ao tocar em qualquer lugar do card
        // (implementado via um botão "Próximo" — adapte conforme seu layout)
        // Aqui usamos o próprio campo de quantidade: ao confirmar teclado → avança
        binding.editQuantidade.setOnEditorActionListener { _, _, _ ->
            avancarParaPasso2()
            true
        }

        // ── Passo 2 ──

        binding.tvVoltar.setOnClickListener {
            voltarParaPasso1()
        }

        binding.addImageButton.setOnClickListener {
            selecionarImagem()
        }

        binding.editImageButton.setOnClickListener {
            selecionarImagem()
        }

        binding.buttonNext.setOnClickListener {
            if (imageList.isNotEmpty()) {
                currentImageIndex = (currentImageIndex + 1) % imageList.size
                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

        binding.buttonPrev.setOnClickListener {
            if (imageList.isNotEmpty()) {
                currentImageIndex =
                    if (currentImageIndex - 1 < 0) imageList.size - 1
                    else currentImageIndex - 1
                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

        binding.buttonFinalizar.setOnClickListener {
            finalizarDoacao()
        }

        binding.btnProximo.setOnClickListener {
            avancarParaPasso2()
        }
    }

    // ── Navegação entre passos ───────────────────────────────────────────────

    /**
     * Valida a quantidade e avança para o Passo 2 (fotos).
     * Chamada quando o usuário clica em "Próximo" ou confirma o teclado.
     */
    fun avancarParaPasso2() {
        val qtdTexto = binding.editQuantidade.text.toString().trim()
        if (qtdTexto.isEmpty()) {
            Toast.makeText(requireContext(), "Digite uma quantidade", Toast.LENGTH_SHORT).show()
            return
        }

        binding.layoutPasso1.visibility = View.GONE
        binding.layoutPasso2.visibility = View.VISIBLE
    }

    private fun voltarParaPasso1() {
        binding.layoutPasso2.visibility = View.GONE
        binding.layoutPasso1.visibility = View.VISIBLE
    }

    // ── Seleção de imagem ────────────────────────────────────────────────────

    private fun selecionarImagem() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    // ── Finalizar doação ─────────────────────────────────────────────────────

    private fun finalizarDoacao() {
        val quantidadeTexto = binding.editQuantidade.text.toString()
        if (quantidadeTexto.isEmpty()) {
            Toast.makeText(requireContext(), "Digite uma quantidade", Toast.LENGTH_SHORT).show()
            return
        }

        val quantidade = quantidadeTexto.toDouble()
        val categoria  = binding.spinnerCategoria.selectedItem?.toString()
            ?: arguments?.getString("categoria_campanha")
            ?: "Roupa"

        val imagensBase64 = imageList.map { uriToBase64(it) }

        val doacao = DoacaoData(
            imagens      = imagensBase64,
            quantidade   = quantidade,
            categoria    = categoria,
            campanha_id  = campanha_id,
            campanha_nome = campanha_nome
        )

        salvarNoBanco(doacao)
    }

    // ── Firebase ─────────────────────────────────────────────────────────────

    private fun salvarNoBanco(item: DoacaoData) {
        val uid = auth.currentUser?.uid ?: return

        database.child("doacoes").child(uid).push().setValue(item)
            .addOnSuccessListener {
                atualizarEstatisticasUsuario(uid, item.categoria, item.quantidade) { _, _ ->
                    atualizarQuantidadeCampanha(item.quantidade) {
                        val usuarioRef = database.child("usuarios").child(uid)
                        usuarioRef.get().addOnSuccessListener { snapshot ->
                            val alimentos  = snapshot.child("alimentos").getValue(Int::class.java)  ?: 0
                            val roupas     = snapshot.child("roupas").getValue(Int::class.java)     ?: 0
                            val brinquedos = snapshot.child("brinquedos").getValue(Int::class.java) ?: 0
                            val total      = snapshot.child("total_doacoes").getValue(Double::class.java)?.toInt() ?: 0

                            ConquistasManager.verificarConquistas(
                                requireContext(), uid, alimentos, roupas, brinquedos, total
                            )
                        }
                    }
                }
            }
    }

    private fun atualizarEstatisticasUsuario(
        uid: String,
        categoria: String,
        quantidade: Double,
        onFinish: (Int?, Int) -> Unit
    ) {
        val usuarioRef = database.child("usuarios").child(uid)
        usuarioRef.child("total_doacoes").get().addOnSuccessListener { totalSnapshot ->
            val totalAtual = totalSnapshot.getValue(Double::class.java) ?: 0.0
            usuarioRef.child("total_doacoes").setValue(totalAtual + quantidade)
            atualizarTotalCategoria(usuarioRef, categoria, quantidade, onFinish)
        }
    }

    private fun atualizarTotalCategoria(
        usuarioRef: DatabaseReference,
        categoria: String,
        quantidade: Double,
        onFinish: (Int?, Int) -> Unit
    ) {
        val campo = when (categoria) {
            "Alimento"  -> "alimentos"
            "Brinquedo" -> "brinquedos"
            "Roupa"     -> "roupas"
            else        -> { onFinish(null, 0); return }
        }
        usuarioRef.child(campo).get().addOnSuccessListener { snapshot ->
            val novoValor = (snapshot.getValue(Int::class.java) ?: 0) + quantidade.toInt()
            usuarioRef.child(campo).setValue(novoValor).addOnSuccessListener {
                onFinish(null, novoValor)
            }
        }
    }

    private fun atualizarQuantidadeCampanha(quantidadeDoada: Double, onFinish: () -> Unit) {
        if (campanha_id.isEmpty() || criadorId.isEmpty()) { onFinish(); return }

        val campanhaRef = database.child("campanhas").child(criadorId).child(campanha_id)
        campanhaRef.child("quantidade_atual").get().addOnSuccessListener { snapshot ->
            val nova = (snapshot.getValue(Double::class.java) ?: 0.0) + quantidadeDoada
            campanhaRef.child("quantidade_atual").setValue(nova).addOnSuccessListener { onFinish() }
        }
    }

    // ── ActivityResult (legado — mantido para compatibilidade) ───────────────

    @Deprecated("Use ActivityResultLauncher")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageList.add(uri)
                currentImageIndex = imageList.size - 1
                binding.imagePreview.setImageURI(uri)

                // Mostra a segunda miniatura se houver 2+ imagens
                if (imageList.size >= 2) {
                    binding.imagePreview2.visibility = View.VISIBLE
                    binding.imagePreview2.setImageURI(imageList[0])
                }

                // Mostra botões de navegação se houver 2+ imagens
                binding.buttonPrev.visibility = if (imageList.size > 1) View.VISIBLE else View.GONE
                binding.buttonNext.visibility = if (imageList.size > 1) View.VISIBLE else View.GONE
            }
        }
    }

    // ── Utilitários ──────────────────────────────────────────────────────────

    private fun uriToBase64(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}