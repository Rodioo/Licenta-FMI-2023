package com.antoniofalcescu.licenta.profile.recentlyPlayedTracks

import com.antoniofalcescu.licenta.profile.tracks.TrackItem
import java.time.Instant
import kotlin.math.round

data class RecentlyPlayedTrack(
    val items: List<RecentlyPlayedTrackItem>?,
    val error: ErrorBody?
)

data class RecentlyPlayedTrackItem(
    val track: TrackItem,
    val played_at: String,
    @Transient var elapsedHours: String = "0h"
) {
    init {
        val millisecondsToHours: Double = 60 * 60 * 1_000.0
        val playedAtTimestamp = Instant.parse(played_at).toEpochMilli()
        val currentTimestamp = System.currentTimeMillis()
        val elapsedHoursAux = round((currentTimestamp - playedAtTimestamp) / millisecondsToHours).toInt()
        elapsedHours = "${elapsedHoursAux}h"
    }
}

data class ErrorBody(
    val status: Int,
    val message: String
)
