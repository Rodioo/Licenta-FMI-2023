package com.antoniofalcescu.licenta.question

data class Question(
    val id: String,
    val imageUrl: String,
    val previewUrl: String,
    val correctAnswer: String,
    val answers: List<String>,
) {
    constructor() : this("", "", "", "", emptyList())
}
