package com.antoniofalcescu.licenta.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.antoniofalcescu.licenta.profile.artists.Artist
import com.antoniofalcescu.licenta.profile.currentlyPlayingTrack.CurrentlyPlayingTrack
import com.antoniofalcescu.licenta.profile.recentlyPlayedTracks.RecentlyPlayedTrack
import com.antoniofalcescu.licenta.profile.tracks.Track
import com.antoniofalcescu.licenta.repository.GuessifyApi
import kotlinx.coroutines.*

class ProfileViewModel(val accessToken: String): ViewModel() {

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile>
        get() = _profile

    private val _track = MutableLiveData<Track>()
    val track: LiveData<Track>
        get() = _track

    private val _artist = MutableLiveData<Artist>()
    val artist: LiveData<Artist>
        get() = _artist

    private val _recentlyPlayed = MutableLiveData<RecentlyPlayedTrack>()
    val recentlyPlayed: LiveData<RecentlyPlayedTrack>
        get() = _recentlyPlayed

    private val _currentTrack = MutableLiveData<CurrentlyPlayingTrack>()
    val currentTrack: LiveData<CurrentlyPlayingTrack>
        get() = _currentTrack

    init {
        getCurrentUserProfile()
        getCurrentUserTopTracks()
        getCurrentUserTopArtists()
        getCurrentUserRecentlyPlayedTracks()
        getCurrentUserCurrentlyPlayingTrack()
    }

    private fun getCurrentUserProfile() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserProfile("Bearer $accessToken")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserProfile_SUCCESS", response.body().toString())
                    _profile.value = response.body()
                } else {
                    Log.e("getCurrentUserProfile_FAILURE", response.code().toString())
                    Log.e("getCurrentUserProfile_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    private fun getCurrentUserTopTracks() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserTopTracks("Bearer $accessToken")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserTopTracks_SUCCESS", response.body().toString())
                    _track.value = response.body()
                } else {
                    Log.e("getCurrentUserTopTracks_FAILURE", response.code().toString())
                    Log.e("getCurrentUserTopTracks_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    private fun getCurrentUserTopArtists() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserTopArtists("Bearer $accessToken")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserTopArtists_SUCCESS", response.body().toString())
                    _artist.value = response.body()
                } else {
                    Log.e("getCurrentUserTopArtists_FAILURE", response.code().toString())
                    Log.e("getCurrentUserTopArtists_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    private fun getCurrentUserRecentlyPlayedTracks() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserRecentlyPlayedTracks("Bearer $accessToken")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserRecentlyPlayedTracks_SUCCESS", response.body().toString())
                    _recentlyPlayed.value = response.body()
                } else {
                    Log.e("getCurrentUserRecentlyPlayedTracks_FAILURE", response.code().toString())
                    Log.e("getCurrentUserRecentlyPlayedTracks_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    private fun getCurrentUserCurrentlyPlayingTrack() {
        coroutineScope.launch {
            while(true) {
                val response = GuessifyApi.retrofitService.getCurrentUserCurrentlyPlayingTrack("Bearer $accessToken")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.e("getCurrentUserCurrentlyPlayedTrack_SUCCESS", response.body().toString())
                        _currentTrack.value = response.body()
                    } else {
                        Log.e("getCurrentUserCurrentlyPlayedTrack_FAILURE", response.code().toString())
                        Log.e("getCurrentUserCurrentlyPlayedTrack_FAILURE", response.errorBody().toString())
                    }
                }
                delay(3000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}