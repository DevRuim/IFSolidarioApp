package com.ifpr.ifsolidarioapp.ui.conquista

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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

        binding.lottieBackground.post {

            val lottieResId = resources.getIdentifier(lottieResName, "raw", packageName)

            if (lottieResId != 0) {
                binding.lottieBackground.setAnimation(lottieResId)
                binding.lottieBackground.cancelAnimation()
                binding.lottieBackground.progress = 0f
                binding.lottieBackground.visibility = View.VISIBLE
                binding.lottieBackground.playAnimation()
            }
        }

        if (insigniaDrawable != 0) {
            binding.imageInsignia.setImageResource(insigniaDrawable)
        }

        Handler(Looper.getMainLooper()).postDelayed({

            ConquistasManager.conquistaFinalizada(this)


            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("abrirPerfil", true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
            finishAffinity()

        }, tempoDuracao)
    }
}