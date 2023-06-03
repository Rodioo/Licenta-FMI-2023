package com.antoniofalcescu.licenta.home

data class User(
    val id_spotify: String,
    val token: String,
    val name: String,
    val image_url: String
) {
    constructor() : this("", "", "", "")
}
