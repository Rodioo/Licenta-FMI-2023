package com.antoniofalcescu.licenta.profile.artists

import com.antoniofalcescu.licenta.utils.SpotifyImage
import com.antoniofalcescu.licenta.utils.SpotifyUrl

data class Artist(
    val items: List<ArtistItem>
)

data class ArtistItem(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>,
    val external_urls: SpotifyUrl
)
