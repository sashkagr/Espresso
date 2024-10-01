package com.espresso.app.articles

data class Article(
    val userId: String,
    val title: String,
    val content: String,
    val authorId: String,
    val isPublished: Boolean = false // Новое поле
)
