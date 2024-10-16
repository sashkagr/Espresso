package com.espresso.app.articles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import com.google.android.gms.tasks.Tasks
import com.google.firebase.appcheck.FirebaseAppCheck

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.nio.charset.Charset

class UserArticlesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var listView: ListView
    private lateinit var adapter: ArticleAdapter
    private lateinit var userId: String
    private lateinit var statusTextView: TextView
    private lateinit var titlePage: TextView
    private lateinit var ruSwitch: Switch // Изменено на lateinit
    var RU = false  // Флаг для отслеживания языка

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_articles)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        listView = findViewById(R.id.articlesListView)
        statusTextView = findViewById(R.id.editTextTextEmailAddress)
        userId = auth.currentUser?.uid ?: ""
        titlePage = findViewById(R.id.userNameTextView)
        val cup: ImageButton = findViewById(R.id.imageButton6)
        ruSwitch = findViewById(R.id.switch1) // Инициализация после setContentView

        adapter = ArticleAdapter(this, mutableListOf())
        listView.adapter = adapter
        if (listView.isEmpty()) {
            statusTextView.text = "No have articlies"
        }

        fetchPublishedArticles(userId, statusTextView)
        cup.setOnClickListener {
            startActivity(Intent(this, ArticlesListActivity::class.java))
        }
        ruSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                RU = true
                titlePage.text = "НАПИСАТЬ СТАТЬЮ"
                if (listView.isEmpty()) {
                    statusTextView.text = "Нет статей"
                }
                titlePage.text = "Черновики"
            } else {
                RU = false
                if (listView.isEmpty()) {
                    statusTextView.text = "No have articlies"
                }
                titlePage.text = "Draft"
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val articleUri = adapter.getItem(position) ?: return@setOnItemClickListener
            val intent = Intent(this, EditArticleActivity::class.java).apply {
                putExtra("ARTICLE_URL", articleUri.toString())
            }
            startActivity(intent)
        }
    }

    private fun fetchPublishedArticles(userId: String, statusTextView: TextView) {
        val articlesRef = storage.reference.child("articles/private")
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
                    if (count > 0) {
                        statusTextView.text = " "
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error listing files: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
