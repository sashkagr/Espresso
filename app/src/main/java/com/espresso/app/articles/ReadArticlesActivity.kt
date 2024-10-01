package com.espresso.app.articles

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject
import java.nio.charset.Charset


class ReadArticlesActivity : AppCompatActivity() {

        private lateinit var storage: FirebaseStorage
        private lateinit var articleRef: StorageReference
        private lateinit var auth: FirebaseAuth
        private lateinit var titleText: TextView
        private lateinit var contentText: TextView
        private lateinit var saveButton: Button
        private lateinit var deleteButton: Button
        private lateinit var switchRu: Switch
        private var articleUrl: String? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_read_article)

            switchRu = findViewById(R.id.switch1)
            storage = FirebaseStorage.getInstance()
            auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            titleText = findViewById(R.id.titleEditText)
            contentText = findViewById(R.id.contentEditText)
            saveButton = findViewById(R.id.submitButton)
            deleteButton = findViewById(R.id.submitButton3)
            articleUrl = intent.getStringExtra("ARTICLE_URL")
            titleText.movementMethod = ScrollingMovementMethod()
            contentText.movementMethod = ScrollingMovementMethod()
            articleUrl?.let {
                articleRef = storage.getReferenceFromUrl(it)
                // Вы можете загрузить содержимое статьи и отобразить его в EditText
                articleRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                    // Загрузка данных о пользователе из файла JSON
                        val jsonStr = String(bytes, Charset.forName("UTF-8"))
                        val jsonObject = JSONObject(jsonStr)

                        // Получение информации о пользователе
                       val title = jsonObject.getString("title")
                       val content = jsonObject.getString("content")
                       val articleUserId = jsonObject.getString("userId")
                    if(user!=null) {
                        val userId = user.uid
                        if (userId.toString() == articleUserId.toString()) {
                            saveButton.visibility = View.VISIBLE
                            saveButton.isEnabled = true

                            deleteButton.visibility = View.VISIBLE
                            deleteButton.isEnabled = true

                            switchRu.visibility = View.VISIBLE
                            switchRu.isEnabled = true
                        }
                    }
                    titleText.setText(title)
                    contentText.setText(content)
                }.addOnFailureListener {
                    Toast.makeText(this, "Error loading article", Toast.LENGTH_SHORT).show()
                }
            }

//            saveButton.setOnClickListener {
//                val title = titleEditText.text.toString()
//                val content = contentEditText.text.toString()
//
//                if (articleUrl != null && title.isNotEmpty() && content.isNotEmpty()) {
//                    val updatedArticleRef = storage.getReferenceFromUrl(articleUrl!!)
//                    updatedArticleRef.putBytes(content.toByteArray())
//                        .addOnSuccessListener {
//                            Toast.makeText(this, "Article updated successfully", Toast.LENGTH_SHORT).show()
//                            finish()
//                        }
//                        .addOnFailureListener {
//                            Toast.makeText(this, "Error updating article", Toast.LENGTH_SHORT).show()
//                        }
//                } else {
//                    Toast.makeText(this, "Please enter a title and content", Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }
