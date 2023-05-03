package com.antoniofalcescu.licenta.profile

data class Profile(
    val country: String,
    val display_name: String,
    val email: String,
    val external_urls: ProfileURL,
    val id: String,
    val images: List<ProfileImage>,
    val product: String
)

data class ProfileURL(
    val spotify: String
)
data class ProfileImage(
    val url: String
)
