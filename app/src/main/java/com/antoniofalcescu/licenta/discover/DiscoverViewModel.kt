package com.antoniofalcescu.licenta.discover

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.profile.artists.Artist
import com.antoniofalcescu.licenta.profile.tracks.Track
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.roomDatabase.LocalDatabase
import kotlinx.coroutines.*

class DiscoverViewModel(application: Application): AndroidViewModel(application) {

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val dbScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val accessTokenDao: AccessTokenDao
    private lateinit var accessToken: AccessToken

    private val _track = MutableLiveData<Track>()
    val track: LiveData<Track>
        get() = _track
    val shuffledTracks = MutableLiveData<Track>()

    private val _artist = MutableLiveData<Artist>()
    val artist: LiveData<Artist>
        get() = _artist
    val shuffledArtists = MutableLiveData<Artist>()


    private val _discoverTrack = MutableLiveData<DiscoverTrack>()
    val discoverTrack: LiveData<DiscoverTrack>
        get() = _discoverTrack

    init {
        accessTokenDao = LocalDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (!(::accessToken.isInitialized) || accessToken.value == null) {
                accessToken = getAccessToken()
            }
            getCurrentUserTopTracks()
            getCurrentUserTopArtists()
        }
    }

    private suspend fun getAccessToken(): AccessToken {
        return withContext(dbScope.coroutineContext) {
            accessTokenDao.get()
        }
    }

    private fun getCurrentUserTopTracks(){
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserTopTracks("Bearer ${accessToken.value}")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _track.value = response.body()
                    Log.e("getCurrentUserTopTracks_DISCOVER_SUCCESS", _track.value.toString())
                } else {
                    Log.e("getCurrentUserTopTracks_DISCOVER_FAILURE", response.code().toString())
                    Log.e("getCurrentUserTopTracks_DISCOVER_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    private fun getCurrentUserTopArtists() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserTopArtists("Bearer ${accessToken.value}")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _artist.value = response.body()
                    Log.e("getCurrentUserTopArtists_DISCOVER_SUCCESS", _track.value.toString())
                } else {
                    Log.e("getCurrentUserTopArtists_DISCOVER_FAILURE", response.code().toString())
                    Log.e("getCurrentUserTopArtists_DISCOVER_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    fun getCurrentUserRecommendations(basedOnTracks: Boolean) {
        coroutineScope.launch {
            val response = if (basedOnTracks) {
                val shuffledItems = _track.value!!.items.shuffled().take(3)
                shuffledTracks.value = Track(items= shuffledItems)
                val tracksId = shuffledTracks.value?.items?.shuffled()?.take(3)?.joinToString(",") { it.id }
                GuessifyApi.retrofitService.getCurrentUserRecommendations(
                    "Bearer ${accessToken.value}",
                    tracksId = tracksId?: ""
                )
            } else {
                val shuffledItems = _artist.value!!.items.shuffled().take(3)
                shuffledArtists.value = Artist(items= shuffledItems)
                val artistsId = _artist.value?.items?.shuffled()?.take(3)?.joinToString(",") { it.id }
                GuessifyApi.retrofitService.getCurrentUserRecommendations(
                    "Bearer ${accessToken.value}",
                    artistsId = artistsId?: ""
                )
            }

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserRecommendations_SUCCESS", response.body().toString())
                    _discoverTrack.value = response.body()
                } else {
                    Log.e("getCurrentUserRecommendations_FAILURE", response.code().toString())
                    Log.e("getCurrentUserRecommendations_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}