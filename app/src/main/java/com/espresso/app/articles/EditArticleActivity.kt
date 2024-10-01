package com.espresso.app.articles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditArticleActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var articleRef: StorageReference
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private var articleUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_article)

        storage = FirebaseStorage.getInstance()
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        saveButton = findViewById(R.id.saveButton)

        articleUrl = intent.getStringExtra("ARTICLE_URL")
        articleUrl?.let {
            articleRef = storage.getReferenceFromUrl(it)
            // Вы можете загрузить содержимое статьи и отобразить его в EditText
            articleRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val content = String(bytes)
                contentEditText.setText(content)
            }.addOnFailureListener {
                Toast.makeText(this, "Error loading article", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            if (articleUrl != null && title.isNotEmpty() && content.isNotEmpty()) {
                val updatedArticleRef = storage.getReferenceFromUrl(articleUrl!!)
                updatedArticleRef.putBytes(content.toByteArray())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Article updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating article", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter a title and content", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
