package com.espresso.app.articles
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.nio.charset.Charset

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var adapter: ArticleAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val user = auth.currentUser
        val userNameTextView: TextView = findViewById(R.id.userNameTextView)
        val writeArticleButton: Button = findViewById(R.id.writeArticleButton)
        val articlesListButton: Button = findViewById(R.id.articlesListButton)
        val editTextTextEmailAddress: TextView = findViewById(R.id.editTextTextEmailAddress)
        val statusTextView: TextView = findViewById(R.id.statusText)
        val textView = findViewById<TextView>(R.id.editTextTextEmailAddress)
        textView.movementMethod = ScrollingMovementMethod()
        val textView1 = findViewById<TextView>(R.id.userNameTextView)
        textView1.movementMethod = ScrollingMovementMethod()
        val ruSwitch: Switch = findViewById(R.id.switch1)
        var RU = false  // Флаг для отслеживания языка
        var userName =""
        var userInfo =""
        var userEmail =""
        val list: ListView = findViewById(R.id.articlesListView)
        adapter = ArticleAdapter(this, mutableListOf())
        list.adapter = adapter
        if (list.isEmpty()) {
            statusTextView.text = "No have articlies"
        }
        list.setOnItemClickListener { _, _, position, _ ->
            val articleUri = adapter.getItem(position) ?: return@setOnItemClickListener
            val intent = Intent(this, ReadArticlesActivity::class.java).apply {
                putExtra("ARTICLE_URL", articleUri.toString())
            }
            startActivity(intent)
        }
        ruSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch включен
                RU = true
                writeArticleButton.text = "НАПИСАТЬ СТАТЬЮ"
                if (list.isEmpty()) {
                    statusTextView.text = "Нет статей"
                }
                editTextTextEmailAddress.text =
                    "Email для связи:\n$userEmail\n\nИнформация об авторе:\n$userInfo"
                articlesListButton.text="Черновики"

            } else {
                // Switch выключен
                RU = false
                writeArticleButton.text = "WRITE ARTICLE"
                if (list.isEmpty()) {
                    statusTextView.text = "No have articlies"
                }
                articlesListButton.text="Draft Articles"
                editTextTextEmailAddress.text =
                    "Email to connect:\n$userEmail\n\nInfo about author:\n$userInfo"
            }
        }

        if (user != null) {
            val userId = user.uid
            fetchPublishedArticles(userId.toString(),statusTextView)

            val storageRef = storage.reference.child("users/$userId/info.json")

            // Загрузка данных о пользователе из файла JSON
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val jsonStr = String(bytes, Charset.forName("UTF-8"))
                val jsonObject = JSONObject(jsonStr)

                // Получение информации о пользователе
                userName = jsonObject.getString("username")
                userInfo = jsonObject.getString("info")
                userEmail = jsonObject.getString("email")
                // Установка значений на экран
                userNameTextView.text = "$userName"

                    if (RU) {
                        editTextTextEmailAddress.text =
                            "Email для связи:\n$userEmail\n\nИнформация об авторе:\n$userInfo"
                    } else {
                        // Switch выключен
                        editTextTextEmailAddress.text =
                            "Email to connect:\n$userEmail\n\nInfo about author:\n$userInfo"
                    }

            }.addOnFailureListener { e ->
                Log.e("UserProfileActivity", "Error loading user data: ${e.message}")
                if (RU) {
                    Toast.makeText(this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            }

            writeArticleButton.setOnClickListener {
                startActivity(Intent(this, WriteArticleActivity::class.java))
            }

            articlesListButton.setOnClickListener {
                startActivity(Intent(this, UserArticlesActivity::class.java))
            }
        } else {
            if (RU) {
                Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error of authorization", Toast.LENGTH_SHORT).show()
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
    private fun fetchPublishedArticles(userId: String,statusTextView: TextView) {
        val articlesRef = storage.reference.child("articles/public")
        var count = 0
        articlesRef.listAll().addOnSuccessListener { listResult ->
            val articles = mutableListOf<Uri>()
            val fileTasks = listResult.items.map { item ->
                val articlesRef1 = storage.reference.child(item.toString().substringAfter(".com/"))
                Log.e("AR:", articlesRef1.toString())
                articlesRef1.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                    val jsonStr = String(bytes, Charset.forName("UTF-8"))
                    val jsonObject = JSONObject(jsonStr)

                    // Получение информации о пользователе
                    val userId1 = jsonObject.getString("userId").toString()
                    Log.e("UserId1:", userId1)

                    Log.e("UserId:", userId)
                    Log.e("Item:", item.toString())
                    if (userId == userId1) {
                        Log.e("Status:", "ok")
                        count++
                        item.downloadUrl.addOnSuccessListener { uri ->
                            articles.add(uri)
                            adapter.clear()
                            adapter.addAll(articles)
                            adapter.notifyDataSetChanged()
                        }.addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Error loading article URL: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        }
                    if(count>0){
                        statusTextView.text=" "
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error listing files: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
