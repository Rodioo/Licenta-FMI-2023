package com.antoniofalcescu.licenta.profile.tracks

import com.antoniofalcescu.licenta.utils.SpotifyImage
import com.antoniofalcescu.licenta.utils.SpotifyUrl

data class Track(
    val items: List<TrackItem>
)

data class TrackItem(
    val album: AlbumInfo,
    val artists: List<ArtistInfo>,
    val id: String,
    val name: String,
    val external_urls: SpotifyUrl
)

data class AlbumInfo(
    val images: List<SpotifyImage>
)

data class ArtistInfo(
    val name: String
)
