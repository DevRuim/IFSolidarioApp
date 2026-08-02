package com.ifpr.ifsolidarioapp.ui.conquista

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.ifsolidarioapp.MainActivity
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.databinding.ActivityConquistaBinding

class ConquistaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConquistaBinding
    private val handler = Handler(Looper.getMainLooper())
    private var terminou = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConquistaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarDados(intent)
    }

    // onNewIntent é chamado quando singleTop impede nova instância
    // (útil para fila de conquistas)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { carregarDados(it) }
    }

    private fun carregarDados(intent: Intent) {
        // Cancela qualquer delayed anterior antes de iniciar novo
        handler.removeCallbacksAndMessages(null)

        val mensagem         = intent.getStringExtra("mensagem")         ?: "Parabéns!"
        val lottieResName    = intent.getStringExtra("lottieResName")    ?: "conquista"
        val insigniaDrawable = intent.getIntExtra("insigniaDrawable", 0)
        val tempoDuracao     = intent.getLongExtra("tempoDuracao", 5000L)

        binding.textMensagem.text = mensagem

        val lottieResId = resources.getIdentifier(lottieResName, "raw", packageName)
        if (lottieResId != 0) {
            binding.lottieBackground.setAnimation(lottieResId)
            binding.lottieBackground.playAnimation()
        }

        if (insigniaDrawable != 0) {
            binding.imageInsignia.setImageResource(insigniaDrawable)
        }

        handler.postDelayed({

            finish()

        }, tempoDuracao)
    }

    private fun navegarParaRanking() {
        // CLEAR_TOP garante que a MainActivity não seja recriada —
        // ela recebe o Intent via onNewIntent e seleciona o Ranking.
        // SINGLE_TOP evita criar uma nova instância da MainActivity.
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("abrirRanking", true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacksAndMessages(null)

        if (!terminou) {
            terminou = true
            ConquistasManager.conquistaFinalizada(applicationContext)
        }
    }
}