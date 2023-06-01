package com.antoniofalcescu.licenta.repository

import com.antoniofalcescu.licenta.discover.DiscoverTrack
import com.antoniofalcescu.licenta.home.AvailableGenre
import com.antoniofalcescu.licenta.profile.Profile
import com.antoniofalcescu.licenta.profile.recentlyPlayedTracks.RecentlyPlayedTrack
import com.antoniofalcescu.licenta.profile.artists.Artist
import com.antoniofalcescu.licenta.profile.currentlyPlayingTrack.CurrentlyPlayingTrack
import com.antoniofalcescu.licenta.profile.tracks.Track
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

private const val BASE_URL = "https://api.spotify.com/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface GuessifyApiService {
    @GET(value= "me")
    suspend fun getCurrentUserProfile(@Header("Authorization") accessToken: String): Response<Profile>

    @GET(value= "me/top/tracks")
    suspend fun getCurrentUserTopTracks(
        @Header("Authorization") accessToken: String,
        @Query("time_range") timeRange: String = "short_term",
    ): Response<Track>

    @GET(value= "me/top/artists")
    suspend fun getCurrentUserTopArtists(
        @Header("Authorization") accessToken: String,
        @Query("time_range") timeRange: String = "short_term",
    ): Response<Artist>

    @GET(value= "me/player/recently-played")
    suspend fun getCurrentUserRecentlyPlayedTracks(
        @Header("Authorization") accessToken: String,
        @Query("after") afterTimestamp: Long,
        @Query("limit") limit: Int = 40
    ): Response<RecentlyPlayedTrack>

    @GET(value= "me/player/currently-playing")
    suspend fun getCurrentUserCurrentlyPlayingTrack(
        @Header("Authorization") accessToken: String,
    ): Response<CurrentlyPlayingTrack>

    @GET(value= "recommendations")
    suspend fun getCurrentUserRecommendations(
        @Header("Authorization") accessToken: String,
        @Query("seed_artists") artistsId: String = "",
        @Query("seed_tracks") tracksId: String = "",
        @Query("limit") limit: Int = 1
    ): Response<DiscoverTrack>

    @GET(value= "browse/categories")
    suspend fun getGenres(
        @Header("Authorization") accessToken: String,
        @Query("limit") limit: Int = 30
    ): Response<AvailableGenre>
}

object GuessifyApi {
    val retrofitService: GuessifyApiService by lazy {
        retrofit.create(GuessifyApiService::class.java)
    }
}