package com.espresso.app.articles

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject
import java.nio.charset.Charset

class ArticleAdapter(context: Context, articles: MutableList<Uri>) :
    ArrayAdapter<Uri>(context, 0, articles) {

    private val storage = FirebaseStorage.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val articleUri = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val titleView: TextView = view.findViewById(R.id.articleTitle)

        // Проверяем, что articleUri не null
        articleUri?.let { uri ->
            // Получаем ссылку на файл в Firebase Storage
            val storageRef = storage.getReferenceFromUrl(uri.toString())

            // Загружаем данные файла
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val jsonStr = String(bytes, Charset.forName("UTF-8"))
                val jsonObject = JSONObject(jsonStr)

                // Получаем информацию о статье
                val title = jsonObject.getString("title")
                titleView.text = title
            }.addOnFailureListener { exception ->
                // Обработка ошибки
                titleView.text = "Error loading article"
                exception.printStackTrace()
            }
        } ?: run {
            titleView.text = "No Title"
        }

        return view
    }
}
