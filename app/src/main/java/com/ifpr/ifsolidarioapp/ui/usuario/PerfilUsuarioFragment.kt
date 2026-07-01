package com.ifpr.ifsolidarioapp.ui.usuario

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.databinding.FragmentPerfilUsuarioBinding
import com.ifpr.ifsolidarioapp.ui.login.LoginActivity
import androidx.navigation.fragment.findNavController

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding
        get() = _binding
            ?: throw IllegalStateException(
                "Binding acessado após onDestroyView()"
            )
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

        carregarDadosUsuario()
        setupClicks()

        return binding.root
    }

    private fun setupClicks() {

        binding.buttonEditarPerfilUsuario.setOnClickListener {
            abrirTelaEditarPerfil()
        }

        binding.buttonEditarPerfilOng.setOnClickListener {
            abrirTelaEditarPerfil()
        }

        binding.buttonHistoricoDoacao.setOnClickListener {

            findNavController().navigate(
                R.id.action_profile_to_historico
            )
        }

        binding.buttonSairUsuario.setOnClickListener {
            signOut()
        }

        binding.buttonSairOng.setOnClickListener {
            signOut()
        }
    }

    private fun abrirTelaEditarPerfil() {
        findNavController().navigate(
            R.id.action_profile_to_editarPerfil
        )
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

            val b = _binding ?: return@addOnSuccessListener

            if (usuarioSnapshot.exists()) {

                b.cardInfoUsuario.visibility = View.VISIBLE
                b.cardConquista.visibility = View.VISIBLE
                b.cardOpcoesUsuario.visibility = View.VISIBLE

                b.cardInfoOng.visibility = View.GONE
                b.cardOpcoesOng.visibility = View.GONE

                carregarDadosDoador(usuarioSnapshot)

            } else {

                ongsRef.get()
                    .addOnSuccessListener { ongSnapshot ->

                        val b2 = _binding ?: return@addOnSuccessListener

                        if (ongSnapshot.exists()) {

                            b2.cardInfoUsuario.visibility = View.GONE
                            b2.cardConquista.visibility = View.GONE
                            b2.cardOpcoesUsuario.visibility = View.GONE

                            b2.cardInfoOng.visibility = View.VISIBLE
                            b2.cardOpcoesOng.visibility = View.VISIBLE

                            carregarDadosOng(ongSnapshot)
                        }
                    }
            }
        }
            .addOnFailureListener {

                if (_binding == null) return@addOnFailureListener

                Toast.makeText(
                    context,
                    "Erro ao carregar usuário",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun carregarDadosDoador(
        snapshot: DataSnapshot
    ) {

        carregarFoto(snapshot)

        val nome = snapshot.child("nome_usuario").getValue(String::class.java) ?: ""
        val email = snapshot.child("email_usuario").getValue(String::class.java) ?: ""
        val telefone = snapshot.child("telefone_usuario").getValue(String::class.java) ?: ""
        val alimentos = snapshot.child("alimentos").getValue(Int::class.java) ?: 0
        val brinquedos = snapshot.child("brinquedos").getValue(Int::class.java) ?: 0
        val roupas = snapshot.child("roupas").getValue(Int::class.java) ?: 0
        val total = snapshot.child("total_doacoes").getValue(Int::class.java) ?: 0

        carregarInformacoes(
            nome,
            email,
            telefone,
            alimentos,
            brinquedos,
            roupas,
            total,
        )

        carregarConquistas(
            alimentos,
            roupas,
            brinquedos,
            total
        )
    }

    private fun proximaMeta(total: Int): Int? {
        val metas = listOf(5, 10, 20, 40)
        return metas.firstOrNull { total < it }
    }

    private fun carregarDadosOng(
        snapshot: DataSnapshot
    ) {

        carregarFoto(snapshot)

        val nome = snapshot.child("nome_ong").getValue(String::class.java) ?: ""
        val email = snapshot.child("email_ong").getValue(String::class.java) ?: ""
        val telefone = snapshot.child("telefone_ong").getValue(String::class.java) ?: ""
        val cnpj = snapshot.child("cnpj").getValue(String::class.java) ?: ""

        carregarInformacoesOng(
            nome,
            email,
            telefone,
            cnpj,
        )
    }

    private fun carregarFoto(snapshot: DataSnapshot) {

        val imagemBase64 = snapshot
            .child("imagemBase64")
            .getValue(String::class.java)

        if (imagemBase64.isNullOrEmpty()) {

            binding.imageViewFoto.setImageResource(
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

            binding.imageViewFoto.setImageBitmap(bitmap)

        } catch (e: Exception) {

            e.printStackTrace()

            binding.imageViewFoto.setImageResource(
                R.drawable.ic_profile_white
            )
        }
    }

    private fun carregarInformacoes(
        nome: String,
        email: String,
        telefone: String,
        alimentos: Int,
        brinquedos: Int,
        roupas: Int,
        total: Int,
    ) {

        binding.textViewName.text = nome
        binding.textViewEmail.text = email
        binding.textViewTelefone.text = telefone

        binding.textViewTotal.text = total.toString()

        binding.textViewAlimentos.text = alimentos.toString()
        binding.textViewBrinquedos.text = brinquedos.toString()
        binding.textViewRoupas.text = roupas.toString()

        val metaAlimentos = proximaMeta(alimentos)
        binding.textViewAlimentosMeta.text =
            if (metaAlimentos != null) "($alimentos/$metaAlimentos)" else "(Máx.)"

        val metaBrinquedos = proximaMeta(brinquedos)
        binding.textViewBrinquedosMeta.text =
            if (metaBrinquedos != null) "($brinquedos/$metaBrinquedos)" else "(Máx.)"

        val metaRoupas = proximaMeta(roupas)
        binding.textViewRoupasMeta.text =
            if (metaRoupas != null) "($roupas/$metaRoupas)" else "(Máx.)"

        // Atualiza as barras de progresso após o layout ser desenhado,
        // pois a largura real do pai só está disponível nesse momento.
        binding.progressAlimentos.post {
            atualizarBarra(binding.progressAlimentos, alimentos, metaAlimentos)
        }
        binding.progressBrinquedos.post {
            atualizarBarra(binding.progressBrinquedos, brinquedos, metaBrinquedos)
        }
        binding.progressRoupas.post {
            atualizarBarra(binding.progressRoupas, roupas, metaRoupas)
        }
    }

    /**
     * Calcula a largura proporcional da barra de progresso.
     *
     * - Se [meta] é null o usuário atingiu o máximo (40) → barra cheia.
     * - Caso contrário: percentual = atual / meta, limitado a [0, 1].
     * - A largura é aplicada diretamente nos LayoutParams da View filha,
     *   usando a largura real do pai (já disponível pois chamamos via .post{}).
     */
    private fun atualizarBarra(barraView: View, atual: Int, meta: Int?) {
        val pai = barraView.parent as? View ?: return
        val larguraPai = pai.width
        if (larguraPai == 0) return

        val percentual = if (meta == null) {
            1f // atingiu o máximo
        } else {
            (atual.toFloat() / meta.toFloat()).coerceIn(0f, 1f)
        }

        val params = barraView.layoutParams
        params.width = (larguraPai * percentual).toInt()
        barraView.layoutParams = params
    }

    private fun carregarInformacoesOng(
        nome: String,
        email: String,
        telefone: String,
        cnpj: String
    ) {

        binding.textViewName.text = nome
        binding.textViewEmailOng.text = email
        binding.textViewTelefoneOng.text = telefone
        binding.textViewCNPJ.text = cnpj

    }

    private fun carregarConquistas(
        alimentos: Int,
        roupas: Int,
        brinquedos: Int,
        total: Int
    ) {

        habilitarPrimeiraConquista(total)

        atualizarConquista(
            alimentos, 5,
            binding.imageViewIconAlimento5,
            R.drawable.ic_alimento_5_enabled,
            R.drawable.ic_alimento_5_disabled
        )
        atualizarConquista(
            alimentos, 10,
            binding.imageViewIconAlimento10,
            R.drawable.ic_alimento_10_enabled,
            R.drawable.ic_alimento_10_disabled
        )
        atualizarConquista(
            alimentos, 20,
            binding.imageViewIconAlimento20,
            R.drawable.ic_alimento_20_enabled,
            R.drawable.ic_alimento_20_disabled
        )
        atualizarConquista(
            alimentos, 40,
            binding.imageViewIconAlimento40,
            R.drawable.ic_alimento_40_enabled,
            R.drawable.ic_alimento_40_disabled
        )

        atualizarConquista(
            brinquedos, 5,
            binding.imageViewIconBrinquedo5,
            R.drawable.ic_brinquedo_5_enabled,
            R.drawable.ic_brinquedo_5_disabled
        )
        atualizarConquista(
            brinquedos, 10,
            binding.imageViewIconBrinquedo10,
            R.drawable.ic_brinquedo_10_enabled,
            R.drawable.ic_brinquedo_10_disabled
        )
        atualizarConquista(
            brinquedos, 20,
            binding.imageViewIconBrinquedo20,
            R.drawable.ic_brinquedo_20_enabled,
            R.drawable.ic_brinquedo_20_disabled
        )
        atualizarConquista(
            brinquedos, 40,
            binding.imageViewIconBrinquedo40,
            R.drawable.ic_brinquedo_40_enabled,
            R.drawable.ic_brinquedo_40_disabled
        )

        atualizarConquista(
            roupas, 5,
            binding.imageViewIconRoupa5,
            R.drawable.ic_roupa_5_enabled,
            R.drawable.ic_roupa_5_disabled
        )
        atualizarConquista(
            roupas, 10,
            binding.imageViewIconRoupa10,
            R.drawable.ic_roupa_10_enabled,
            R.drawable.ic_roupa_10_disabled
        )
        atualizarConquista(
            roupas, 20,
            binding.imageViewIconRoupa20,
            R.drawable.ic_roupa_20_enabled,
            R.drawable.ic_roupa_20_disabled
        )
        atualizarConquista(
            roupas, 40,
            binding.imageViewIconRoupa40,
            R.drawable.ic_roupa_40_enabled,
            R.drawable.ic_roupa_40_disabled
        )

        val shouldFocus =
            arguments?.getBoolean("FOCUS_CONQUISTAS") ?: false

        if (shouldFocus) {

            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(
                    0,
                    binding.cardConquista.top
                )
            }

            arguments?.remove("FOCUS_CONQUISTAS")
        }
    }

    private fun habilitarPrimeiraConquista(total: Int) {
        binding.imageViewIconPrimeiraConquista.setImageResource(
            if (total >= 1) R.drawable.ic_primeira_doacao_enabled
            else R.drawable.ic_primeira_doacao_disabled
        )
    }

    private fun atualizarConquista(
        quantidade: Int,
        meta: Int,
        imageView: ImageView,
        enabled: Int,
        disabled: Int
    ) {
        imageView.setImageResource(
            if (quantidade >= meta) enabled else disabled
        )
    }

    private fun signOut() {

        auth.signOut()

        val intent = Intent(
            context,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}