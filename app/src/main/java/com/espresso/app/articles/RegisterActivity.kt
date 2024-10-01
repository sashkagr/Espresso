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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.ByteArrayInputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var RU: Boolean = false  // Флаг для отслеживания языка

    private fun validateInputs(email: String, password: String, username: String, info: String): Boolean {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (RU) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        if (password.length < 6) {
            if (RU) {
                Toast.makeText(this, "Пароль должен содержать как минимум 6 символов", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        if (username.isEmpty()) {
            if (RU) {
                Toast.makeText(this, "Введите уникальный ник", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter a unique username", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        if (info.isEmpty()) {
            if (RU) {
                Toast.makeText(this, "Введите информацию о себе", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter information about yourself", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val infoEditText: EditText = findViewById(R.id.infoEditText)
        val registerButton: Button = findViewById(R.id.registerButton)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val languageSwitch: Switch = findViewById(R.id.switch1)

        // Обработчик для переключателя языка
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                RU = true
                emailEditText.hint = "Введите email:"
                passwordEditText.hint = "Введите пароль:"
                usernameEditText.hint = "Введите ник:"
                infoEditText.hint = "Введите информацию о себе:"
                registerButton.text = "ЗАРЕГИСТРИРОВАТЬСЯ"
            } else {
                RU = false
                emailEditText.hint = "Enter email:"
                passwordEditText.hint = "Enter password:"
                usernameEditText.hint = "Enter username:"
                infoEditText.hint = "Enter info about yourself:"
                registerButton.text = "SIGN UP"
            }
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val username = usernameEditText.text.toString()
            val info = infoEditText.text.toString()

            if (validateInputs(email, password, username, info)) {
                progressBar.visibility = View.VISIBLE
                register(email, password, username, info, progressBar)
            }
        }
    }

    private fun register(email: String, password: String, username: String, info: String, progressBar: ProgressBar) {
        Log.d("RegisterActivity", "Attempting to register user with email: $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "Registration task completed")
                    val userId = auth.currentUser
                    val user = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "info" to info
                    )
                    if (userId != null && !username.equals(null) && !email.equals(null) && !info.equals(null)) {
                        uploadUser(userId.uid, user)
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Log.e("RegisterActivity", "Registration failed: ${task.exception?.message}")
                    Toast.makeText(
                        this,
                        if (RU) "Ошибка регистрации: ${task.exception?.message}" else "Registration error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun uploadUser(userId: String, user: HashMap<String, String>) {
        val storageRef = storage.reference.child("users/$userId/info.json")

        val jsonObject = JSONObject(user as Map<*, *>?)
        val data = jsonObject.toString().toByteArray()
        val uploadTask = storageRef.putStream(ByteArrayInputStream(data))
        uploadTask.addOnSuccessListener {
            Toast.makeText(this, if (RU) "Регистрация успешна" else "Registration successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, UserProfileActivity::class.java))
            finish()
        }.addOnFailureListener { e ->
            Log.e("RegisterActivity", "Error uploading user data to Storage: ${e.message}")
            Toast.makeText(
                this,
                if (RU) "Ошибка сохранения данных: ${e.message}" else "Error saving data: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
