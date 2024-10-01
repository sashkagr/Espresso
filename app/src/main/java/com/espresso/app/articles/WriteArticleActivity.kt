
package com.espresso.app.articles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.ByteArrayInputStream

class WriteArticleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    var RU = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_article)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val titleEditText: EditText = findViewById(R.id.titleEditText)
        val contentEditText: EditText = findViewById(R.id.contentEditText)
        val submitButton: Button = findViewById(R.id.submitButton)
        val draftButton: Button = findViewById(R.id.submitButton3)
        val ruSwitch: Switch = findViewById(R.id.switch1)

        ruSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                RU = true
                titleEditText.hint = "Заголовок"
                contentEditText.hint = "Содержание"
                submitButton.text = "ОПУБЛИКОВАТЬ"
                draftButton.text = "В ЧЕРНОВИК"
            } else {
                RU = false
                titleEditText.hint = "Title"
                contentEditText.hint = "Content"
                submitButton.text = "SUBMIT"
                draftButton.text = "DRAFT"
            }
        }
        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val user = auth.currentUser

            if (user != null && title.isNotEmpty() && content.isNotEmpty()) {
                uploadArticle(title, content, user.uid,true)
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
                uploadArticle(title, content, user.uid,false)
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
}

    private fun uploadArticle(title: String, content: String, userId: String, isPublished: Boolean) {
        val articleId = userId + "_" + System.currentTimeMillis()
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

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_FILE_PICK = 2
    }
}