package com.espresso.app.articles

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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


class ArticlesListActivity : AppCompatActivity() {
//    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var adapter: ArticleAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_list)

//        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

//        val user = auth.currentUser
//        val writeArticleButton: Button = findViewById(R.id.writeArticleButton)
//        val articlesListButton: Button = findViewById(R.id.articlesListButton)
//        val editTextTextEmailAddress: TextView = findViewById(R.id.editTextTextEmailAddress)
        val statusTextView: TextView = findViewById(R.id.statusText)
//        val textView = findViewById<TextView>(R.id.editTextTextEmailAddress)
//        textView.movementMethod = ScrollingMovementMethod()
        val textView1 = findViewById<TextView>(R.id.userNameTextView)
//        textView1.movementMethod = ScrollingMovementMethod()
        val home: ImageButton = findViewById(R.id.imageButton4)

        val ruSwitch: Switch = findViewById(R.id.switch1)
        var RU = false  // Флаг для отслеживания языка
//        var userName =""
//        var userInfo =""
//        var userEmail =""
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
        fetchPublishedArticles(statusTextView)
        ruSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch включен
                RU = true
                textView1.text = "Статьи"
                if (list.isEmpty()) {
                    statusTextView.text = "Нет статей"
                }

            } else {
                // Switch выключен
                RU = false
                textView1.text = "Articles"
                if (list.isEmpty()) {
                    statusTextView.text = "No have articlies"
                }

            }
        }
        home.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
        

    }
    private fun fetchPublishedArticles(statusTextView: TextView) {
        val articlesRef = storage.reference.child("articles/public")
        articlesRef.listAll().addOnSuccessListener { listResult ->
            val articles = mutableListOf<Uri>()
            val fileTasks = listResult.items.map { item ->
                        item.downloadUrl.addOnSuccessListener { uri ->
                            articles.add(uri)
                            adapter.clear()
                            adapter.addAll(articles)
                            adapter.notifyDataSetChanged()
                            statusTextView.text=" "
                        }.addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Error loading article URL: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error listing files: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

}