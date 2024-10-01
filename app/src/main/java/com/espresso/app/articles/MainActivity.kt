package com.espresso.app.articles

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Initialize UI elements after setting the content view
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerButton: Button = findViewById(R.id.registerButton)
        val ruSwitch: Switch = findViewById(R.id.switch1)
        var RU = false
        progressBar = findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateInputs(email, password,RU)) {
                progressBar.visibility = View.VISIBLE
                signIn(email, password,RU)
            }
        }

        ruSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch включен
                RU = true
                emailEditText.hint = "Введите email:"
                passwordEditText.hint = "Введите пароль:"
                loginButton.text = "ВОЙТИ"
                registerButton.text = "ЗАРЕГИСТРИРОВАТЬСЯ"
            } else {
                // Switch выключен
                RU = false
                emailEditText.hint = "Enter email:"
                passwordEditText.hint = "Enter password:"
                loginButton.text = "LOG IN"
                registerButton.text = "SIGN UP"
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))

        }
    }

    private fun validateInputs(email: String, password: String, RU: Boolean): Boolean {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if(RU) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Enter correct email", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        if (password.length < 6) {
            if(RU) {
                Toast.makeText(
                    this,
                    "Пароль должен содержать как минимум 6 символов",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                Toast.makeText(
                    this,
                    "Password should consist of 6 symbols",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return false
        }
        return true
    }

    private fun signIn(email: String, password: String, RU: Boolean) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d("MainActivity", "signInWithEmail:success")
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    finish()
                } else {
                    Log.w("MainActivity", "signInWithEmail:failure", task.exception)
                    if (RU) {
                        Toast.makeText(
                            this,
                            "Ошибка авторизации: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        Toast.makeText(
                            this,
                            "Error authorization: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }


}
