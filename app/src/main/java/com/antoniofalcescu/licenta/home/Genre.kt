package com.antoniofalcescu.licenta.home

data class AvailableGenre(
    val categories: Genre
)

data class Genre(
    val items: List<GenreItem>
)

data class GenreItem(
    val icons: List<GenreIcon>,
    val id: String,
    val name: String
)

data class GenreIcon(
    val url: String
)