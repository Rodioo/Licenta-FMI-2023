package com.antoniofalcescu.licenta.profile

import com.antoniofalcescu.licenta.utils.SpotifyImage
import com.antoniofalcescu.licenta.utils.SpotifyUrl

data class Profile(
    val country: String,
    val display_name: String,
    val email: String,
    val external_urls: SpotifyUrl,
    val id: String,
    val images: List<SpotifyImage>,
    val product: String
)
