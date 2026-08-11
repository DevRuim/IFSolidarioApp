package com.ifpr.ifsolidarioapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.MainActivity
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.ui.usuario.CadastroUsuarioActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        emailEditText    = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton      = findViewById(R.id.button_login)
        registerLink     = findViewById(R.id.registerLink)
        btnGoogleSignIn  = findViewById(R.id.btnGoogleSignIn)

        registerLink.setOnClickListener {
            startActivity(Intent(this, CadastroUsuarioActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email    = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signInEmail(email, password)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        btnGoogleSignIn.setOnClickListener { signInGoogle() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login com e-mail/senha
    // ─────────────────────────────────────────────────────────────────────────

    private fun signInEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    irParaMainActivity()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login com Google
    // ─────────────────────────────────────────────────────────────────────────

    private fun signInGoogle() {
        // Força o seletor de conta sempre aparecer
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            @Suppress("DEPRECATION")
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Legado")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                autenticarComGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Falha no login com Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Tenta autenticar com a credencial Google.
     *
     * Casos possíveis:
     * A) Sucesso direto → verifica se já tem nó no banco.
     *    A1) Tem nó (usuario ou ong) → apenas loga.
     *    A2) Não tem nó → cria nó de usuário (cadastro automático).
     *
     * B) Falha com "account-exists-with-different-credential" →
     *    o e-mail já existe com email/senha. Pede a senha ao usuário,
     *    faz login email/senha e linka o Google à conta existente.
     */
    private fun autenticarComGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser!!
                    verificarECriarContaSeNecessario(user, account)
                } else {
                    val errorCode = (task.exception as? com.google.firebase.auth.FirebaseAuthException)
                        ?.errorCode ?: ""

                    if (errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") {
                        // E-mail já cadastrado com email/senha — pede a senha para linkar
                        val email = account.email ?: ""
                        pedirSenhaParaLinkarGoogle(email, account)
                    } else {
                        Log.w(TAG, "signInWithGoogle:failure", task.exception)
                        Toast.makeText(this, "Falha na autenticação com Google", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    /**
     * Verifica se o usuário autenticado já tem um nó no Realtime Database.
     * - Se sim: apenas loga normalmente.
     * - Se não: cria o nó de usuário com os dados do Google (cadastro automático).
     */
    private fun verificarECriarContaSeNecessario(
        user: FirebaseUser,
        account: GoogleSignInAccount
    ) {
        val uid         = user.uid
        val usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
        val ongsRef     = FirebaseDatabase.getInstance().getReference("ongs").child(uid)

        usuariosRef.get().addOnSuccessListener { usuarioSnap ->
            if (usuarioSnap.exists()) {
                // Já tem conta de doador → só loga
                Log.d(TAG, "Conta de doador existente, logando.")
                irParaMainActivity()
                return@addOnSuccessListener
            }

            ongsRef.get().addOnSuccessListener { ongSnap ->
                if (ongSnap.exists()) {
                    // Já tem conta de ONG → só loga
                    Log.d(TAG, "Conta de ONG existente, logando.")
                    irParaMainActivity()
                    return@addOnSuccessListener
                }

                // Nenhum nó encontrado → cadastro automático como doador
                Log.d(TAG, "Nenhuma conta encontrada, criando usuário automaticamente.")
                criarUsuarioAutomatico(uid, account)
            }.addOnFailureListener {
                // Não conseguiu checar ONG, loga mesmo assim
                irParaMainActivity()
            }
        }.addOnFailureListener {
            // Não conseguiu checar usuário, loga mesmo assim
            irParaMainActivity()
        }
    }

    /**
     * Cria o nó do usuário no Realtime Database usando os dados vindos do Google.
     * Os campos seguem a mesma estrutura usada no CadastroUsuarioActivity.
     */
    private fun criarUsuarioAutomatico(uid: String, account: GoogleSignInAccount) {
        val dados = mapOf(
            "nome_usuario"     to (account.displayName ?: ""),
            "email_usuario"    to (account.email       ?: ""),
            "telefone_usuario" to "",
            "imagemBase64"     to "",   // sem foto local; pode ser populado depois
            "alimentos"        to 0,
            "brinquedos"       to 0,
            "roupas"           to 0,
            "total_doacoes"    to 0.0
        )

        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .setValue(dados)
            .addOnSuccessListener {
                Log.d(TAG, "Usuário criado automaticamente via Google.")
                Toast.makeText(
                    this,
                    "Bem-vindo, ${account.displayName ?: ""}! Conta criada com sucesso.",
                    Toast.LENGTH_SHORT
                ).show()
                irParaMainActivity()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao criar usuário automático", e)
                // Mesmo com erro no banco, o Auth foi bem-sucedido — deixa logar
                irParaMainActivity()
            }
    }

    /**
     * Situação 2: o e-mail já existe com email/senha no Firebase Auth.
     *
     * Preenche automaticamente o campo de e-mail e exibe uma mensagem
     * pedindo que o usuário informe sua senha para vincular o Google à conta.
     * Após o login email/senha, linka a credencial Google para que o usuário
     * passe a poder usar os dois métodos.
     */
    private fun pedirSenhaParaLinkarGoogle(
        email: String,
        account: GoogleSignInAccount
    ) {
        // Pré-preenche o campo e-mail para o usuário ver
        emailEditText.setText(email)
        passwordEditText.requestFocus()

        Toast.makeText(
            this,
            "Este e-mail já está cadastrado. Digite sua senha para vincular o Google à sua conta.",
            Toast.LENGTH_LONG
        ).show()

        // Substitui temporariamente o clique do botão "Entrar"
        // para executar o fluxo de linkagem ao invés do login normal.
        loginButton.setOnClickListener {
            val senha = passwordEditText.text.toString()
            if (senha.isEmpty()) {
                Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vincularGoogleAContaExistente(email, senha, account)
        }
    }

    /**
     * Faz login com email/senha e linka a credencial Google à conta existente.
     * Após a linkagem o usuário pode logar com os dois métodos.
     */
    private fun vincularGoogleAContaExistente(
        email: String,
        senha: String,
        account: GoogleSignInAccount
    ) {
        val emailCredential = EmailAuthProvider.getCredential(email, senha)

        firebaseAuth.signInWithCredential(emailCredential)
            .addOnSuccessListener { result ->
                val googleCredential = GoogleAuthProvider.getCredential(account.idToken, null)

                result.user?.linkWithCredential(googleCredential)
                    ?.addOnSuccessListener {
                        Log.d(TAG, "Google vinculado com sucesso à conta existente.")
                        Toast.makeText(
                            this,
                            "Google vinculado à sua conta com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Restaura o comportamento normal do botão
                        restaurarBotaoLogin()
                        irParaMainActivity()
                    }
                    ?.addOnFailureListener { e ->
                        Log.w(TAG, "Erro ao vincular Google", e)
                        Toast.makeText(
                            this,
                            "Erro ao vincular Google. Tente novamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                        restaurarBotaoLogin()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show()
            }
    }

    /** Restaura o comportamento padrão do botão "Entrar" após o fluxo de linkagem. */
    private fun restaurarBotaoLogin() {
        loginButton.setOnClickListener {
            val email    = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signInEmail(email, password)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun irParaMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}