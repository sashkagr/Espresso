
package com.espresso.app.articles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

class EditArticleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var articleRef: StorageReference
    private var articleUrl: String? = null

    var RU = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_article)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        articleUrl = intent.getStringExtra("ARTICLE_URL")

        val titleEditText: EditText = findViewById(R.id.titleEditText)
        val contentEditText: EditText = findViewById(R.id.contentEditText)
        val submitButton: Button = findViewById(R.id.submitButton)
        val draftButton: Button = findViewById(R.id.submitButton3)
        val delete: Button = findViewById(R.id.submitButton4)
        val ruSwitch: Switch = findViewById(R.id.switch1)
        var title = ""
        var content = ""
        var articleUserId =""
        var isPublished = ""
        var articleId = ""

        articleUrl?.let {
            articleRef = storage.getReferenceFromUrl(it)
            // Вы можете загрузить содержимое статьи и отобразить его в EditText
            articleRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                // Загрузка данных о пользователе из файла JSON
                val jsonStr = String(bytes, Charset.forName("UTF-8"))
                val jsonObject = JSONObject(jsonStr)

                // Получение информации о пользователе
                title = jsonObject.getString("title")
                content = jsonObject.getString("content")
                articleUserId = jsonObject.getString("userId")
                isPublished = jsonObject.getString("isPublished")
                articleId = jsonObject.getString("articleId")
                titleEditText.setText(title)
                contentEditText.setText(content)
            }.addOnFailureListener {
                Toast.makeText(this, "Error loading article", Toast.LENGTH_SHORT).show()
            }
        }

        ruSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                RU = true
                titleEditText.hint = "Заголовок"
                contentEditText.hint = "Содержание"
                submitButton.text = "ОПУБЛИКОВАТЬ"
                draftButton.text = "СОХРАНИТЬ"
                delete.text = "УДАЛИТЬ"

            } else {
                RU = false
                titleEditText.hint = "Title"
                contentEditText.hint = "Content"
                submitButton.text = "SUBMIT"
                draftButton.text = "SAVE"
                delete.text = "DELETE"

            }
        }


        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val user = auth.currentUser
            articleUrl?.let { it1 -> deleteArticle(it1) }
            if (user != null && title.isNotEmpty() && content.isNotEmpty()) {
                if(isPublished=="false") {
                    uploadArticle(title, content, user.uid, true, articleId)
                }
                else{
                    updateArticle(articleUrl.toString(),title, content, user.uid, true,articleId)

                }
            } else {
                if(RU) {
                    Toast.makeText(
                        this,
                        "Пожалуйста, введите заголовок и содержимое",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else{
                    Toast.makeText(
                        this,
                        "Please, enter title and content",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        draftButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val user = auth.currentUser

            if (user != null && title.isNotEmpty() && content.isNotEmpty()) {
                if(isPublished=="false") {
                    updateArticle(articleUrl.toString(),title, content, user.uid, false,articleId)
                }
                else{
                    updateArticle(articleUrl.toString(), title, content, user.uid, true,articleId)

                }
            } else {
                if(RU) {
                    Toast.makeText(
                        this,
                        "Пожалуйста, введите заголовок и содержимое",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else{
                    Toast.makeText(
                        this,
                        "Please, enter title and content",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        delete.setOnClickListener {
            articleUrl?.let { it1 -> deleteArticle(it1) }
        }
    }

    private fun uploadArticle(title: String, content: String, userId: String, isPublished: Boolean, articleId: String) {
        var status = ""
        if(isPublished) {
            status = "public"
        }
        else{
            status = "private"
        }
        val storageRef = storage.reference.child("articles/$status/$articleId.json")

        val article = hashMapOf(
            "userId" to userId,
            "articleId" to articleId,
            "title" to title,
            "content" to content,
            "isPublished" to isPublished
        )
        val jsonObject = JSONObject(article as Map<*, *>?)
        val data = jsonObject.toString().toByteArray()
        val uploadTask = storageRef.putStream(ByteArrayInputStream(data))
        uploadTask.addOnSuccessListener {
            Toast.makeText(this, if (RU) "Сохранено" else "Save", Toast.LENGTH_SHORT).show()
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
    private fun deleteArticle(articleUrl: String) {
        // Получаем StorageReference для статьи по её URL
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(articleUrl)

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

    private fun updateArticle(articleUrl: String, newTitle: String, newContent: String, userId: String, isPublished: Boolean,articleId: String) {
        // Получаем StorageReference для статьи по её URL
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(articleUrl)

        // Подготавливаем обновленные данные статьи
        val updatedArticle = hashMapOf(
            "userId" to userId,
            "articleId" to articleId,
            "title" to newTitle,
            "content" to newContent,
            "isPublished" to isPublished
        )

        val jsonObject = JSONObject(updatedArticle as Map<*, *>?)
        val data = jsonObject.toString().toByteArray()

        // Обновляем статью (загружаем файл с новыми данными)
        val uploadTask = storageRef.putStream(ByteArrayInputStream(data))
        uploadTask.addOnSuccessListener {
            // Успешное обновление статьи
            Toast.makeText(this, if (RU) "Статья обновлена" else "Article updated", Toast.LENGTH_SHORT).show()

            // Возвращаемся на предыдущий экран (или на другой экран)
            startActivity(Intent(this, UserProfileActivity::class.java))
            finish()
        }.addOnFailureListener { e ->
            // Ошибка при обновлении статьи
            Log.e("UpdateArticle", "Ошибка обновления статьи: ${e.message}")
            Toast.makeText(this, if (RU) "Ошибка обновления статьи: ${e.message}" else "Error updating article: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_FILE_PICK = 2
    }
}