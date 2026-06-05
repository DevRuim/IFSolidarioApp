package com.ifpr.ifsolidarioapp.ui.conquista

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.ifsolidarioapp.databinding.ActivityConquistaBinding

class ConquistaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConquistaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConquistaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mensagem =
            intent.getStringExtra("mensagem") ?: "Parabéns!"

        val lottieResName =
            intent.getStringExtra("lottieResName") ?: "conquista"

        val insigniaDrawable =
            intent.getIntExtra("insigniaDrawable", 0)

        val tempoDuracao =
            intent.getLongExtra("tempoDuracao", 5000L)

        binding.textMensagem.text = mensagem

        val lottieResId = resources.getIdentifier(
            lottieResName,
            "raw",
            packageName
        )

        if (lottieResId != 0) {
            binding.lottieBackground.setAnimation(lottieResId)
            binding.lottieBackground.playAnimation()
        }

        if (insigniaDrawable != 0) {
            binding.imageInsignia.setImageResource(insigniaDrawable)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            setResult(RESULT_OK)
            finish()
        }, tempoDuracao)
    }
}