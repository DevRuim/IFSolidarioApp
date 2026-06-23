package com.ifpr.ifsolidarioapp.ui.conquista

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.ifsolidarioapp.MainActivity
import com.ifpr.ifsolidarioapp.databinding.ActivityConquistaBinding

class ConquistaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConquistaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConquistaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mensagem        = intent.getStringExtra("mensagem")        ?: "Parabéns!"
        val lottieResName   = intent.getStringExtra("lottieResName")   ?: "conquista"
        val insigniaDrawable = intent.getIntExtra("insigniaDrawable", 0)
        val tempoDuracao    = intent.getLongExtra("tempoDuracao", 5000L)

        binding.textMensagem.text = mensagem

        val lottieResId = resources.getIdentifier(lottieResName, "raw", packageName)
        if (lottieResId != 0) {
            binding.lottieBackground.setAnimation(lottieResId)
            binding.lottieBackground.playAnimation()
        }

        if (insigniaDrawable != 0) {
            binding.imageInsignia.setImageResource(insigniaDrawable)
        }

        Handler(Looper.getMainLooper()).postDelayed({

            // Avisa o ConquistasManager que essa conquista foi exibida
            // (ele usará isso para exibir a próxima da fila, se houver)
            ConquistasManager.conquistaFinalizada(this)

            // ── Volta para a MainActivity abrindo o Perfil ──────────────────
            // FLAG_ACTIVITY_CLEAR_TOP fecha todas as Activities empilhadas
            // acima da MainActivity (incluindo esta própria ConquistaActivity)
            // e entrega o Intent para a instância existente da MainActivity.
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("abrirPerfil", true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
            finish()

        }, tempoDuracao)
    }
}