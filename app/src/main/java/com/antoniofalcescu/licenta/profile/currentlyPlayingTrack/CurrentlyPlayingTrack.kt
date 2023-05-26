package com.antoniofalcescu.licenta.profile.currentlyPlayingTrack

import com.antoniofalcescu.licenta.profile.tracks.TrackItem

data class CurrentlyPlayingTrack(
    val emptyResponse: EmptyResponse?,
    val item: TrackItem?
)

data class EmptyResponse(
    val isEmpty: Boolean = true
)
