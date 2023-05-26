package com.antoniofalcescu.licenta.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.profile.artists.Artist
import com.antoniofalcescu.licenta.profile.currentlyPlayingTrack.CurrentlyPlayingTrack
import com.antoniofalcescu.licenta.profile.recentlyPlayedTracks.RecentlyPlayedTrack
import com.antoniofalcescu.licenta.profile.tracks.Track
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.antoniofalcescu.licenta.repository.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDatabase
import kotlinx.coroutines.*

//TODO: Look for a way to reduce the workload on the UI thread
class ProfileViewModel(application: Application): AndroidViewModel(application) {

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val dbScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val accessTokenDao: AccessTokenDao
    private var accessToken: AccessToken? = null

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
        accessTokenDao = AccessTokenDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (accessToken?.value == null) {
                accessToken = getAccessToken()
            }
            Log.e("profile", accessToken.toString())
            getCurrentUserProfile()
            getCurrentUserTopTracks()
            getCurrentUserTopArtists()
            getCurrentUserRecentlyPlayedTracks()
            getCurrentUserCurrentlyPlayingTrack()
        }
    }

    private suspend fun getAccessToken(): AccessToken {
        return withContext(dbScope.coroutineContext) {
            accessTokenDao.get()
        }
    }

    private fun getCurrentUserProfile() {
        coroutineScope.launch {
            Log.e("profile_call", accessToken.toString())
            val response = GuessifyApi.retrofitService.getCurrentUserProfile("Bearer ${accessToken!!.value}")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserProfile_SUCCESS", response.body().toString())
                    _profile.value = response.body()
                    updateToken(false)
                } else {
                    updateToken(true)
                    Log.e("getCurrentUserProfile_FAILURE", response.code().toString())
                    Log.e("getCurrentUserProfile_FAILURE", response.message().toString())
                }
            }
        }
    }

    private fun getCurrentUserTopTracks() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserTopTracks("Bearer ${accessToken!!.value}")
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
            val response = GuessifyApi.retrofitService.getCurrentUserTopArtists("Bearer ${accessToken!!.value}")
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
        val dayInMilliseconds = 24 * 60 * 60 * 1000
        val afterTimestamp = System.currentTimeMillis() - dayInMilliseconds

        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserRecentlyPlayedTracks(
                "Bearer ${accessToken!!.value}",
                afterTimestamp = afterTimestamp
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserRecentlyPlayedTracks_SUCCESS", response.body().toString())
                    _recentlyPlayed.value = response.body()
                } else {
                    Log.e("getCurrentUserRecentlyPlayedTracks_FAILURE", response.code().toString())
                    Log.e("getCurrentUserRecentlyPlayedTracks_FAILURE", response.body()?.error.toString())
                }
            }
        }
    }

    private fun getCurrentUserCurrentlyPlayingTrack() {
        coroutineScope.launch {
            while(true) {
                val response = GuessifyApi.retrofitService.getCurrentUserCurrentlyPlayingTrack("Bearer ${accessToken!!.value}")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.e("getCurrentUserCurrentlyPlayedTrack_SUCCESS", response.body().toString())
                        _currentTrack.value = response.body()
                    } else {
                        Log.e("getCurrentUserCurrentlyPlayedTrack_FAILURE", response.code().toString())
                        Log.e("getCurrentUserCurrentlyPlayedTrack_FAILURE", response.errorBody().toString())
                    }
                }
                delay(5_000L)
            }
        }
    }

    private fun updateToken(needsRefresh: Boolean) {
        dbScope.launch {
            accessTokenDao.updateRefresh(needsRefresh)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}