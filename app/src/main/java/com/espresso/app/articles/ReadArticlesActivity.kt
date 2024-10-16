package com.espresso.app.articles

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
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
        private var articleUrl: String? = null
        private lateinit var auth: FirebaseAuth
        private lateinit var titleText: TextView
        private lateinit var contentText: TextView
        private lateinit var saveButton: Button
        private lateinit var deleteButton: Button
        private lateinit var switchRu: Switch
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_read_article)
            var RU = false
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
            switchRu.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    RU = true
                    saveButton.text = "ИЗМЕНИТЬ"
                    deleteButton.text = "УДАЛИТЬ"

                } else {
                    RU = false
                    saveButton.text = "EDIT"
                    deleteButton.text = "DELETE"

                }
            }
            saveButton.setOnClickListener {
                val intent = Intent(this, EditArticleActivity::class.java).apply {
                    putExtra("ARTICLE_URL", articleUrl.toString())
                }
                startActivity(intent)
            }
            deleteButton.setOnClickListener{
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(articleUrl.toString())

                // Удаляем статью
                storageRef.delete()
                    .addOnSuccessListener {
                        // Успешное удаление
                        Toast.makeText(this, if (RU) "Статья удалена" else "Article deleted", Toast.LENGTH_SHORT).show()

                        // Возвращаемся на предыдущий экран (или на другой экран по вашему выбору)
                        startActivity(Intent(this, UserProfileActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        // Обработка ошибок при удалении
                        Log.e("DeleteArticle", "Ошибка при удалении статьи: ${exception.message}")
                        Toast.makeText(this, if (RU) "Ошибка при удалении статьи: ${exception.message}" else "Error deleting article: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
