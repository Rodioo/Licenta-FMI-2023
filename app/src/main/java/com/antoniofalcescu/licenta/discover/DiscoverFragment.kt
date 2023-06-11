package com.antoniofalcescu.licenta.discover

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.databinding.FragmentDiscoverBinding
import com.antoniofalcescu.licenta.profile.artists.ArtistsAdapter
import com.antoniofalcescu.licenta.profile.tracks.TrackItem
import com.antoniofalcescu.licenta.profile.tracks.TracksAdapter
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing

class DiscoverFragment : Fragment() {

    private lateinit var binding: FragmentDiscoverBinding
    private lateinit var viewModel: DiscoverViewModel
    private lateinit var viewModelFactory: DiscoverViewModelFactory
    private lateinit var discoverUtilsUI: DiscoverUtilsUI

    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var artistsAdapter: ArtistsAdapter

    private var areRecommendedSongsVisible = false
    private var recommendedBasedOn = "none"

    private var mediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDiscoverBinding.inflate(inflater)

        viewModelFactory = DiscoverViewModelFactory(this.requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[DiscoverViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        discoverUtilsUI = DiscoverUtilsUI(this, binding, viewModel, viewLifecycleOwner)

        tracksAdapter = TracksAdapter {
                trackUrl -> openSpotifyLink(trackUrl)
        }
        binding.basedOnTracksRecycler.adapter = tracksAdapter
        binding.basedOnTracksRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.EXTRA_LARGE), Orientation.HORIZONTAL)
        )
        viewModel.shuffledTracks.observe(viewLifecycleOwner) {
                shuffledTracks -> tracksAdapter.submitList(shuffledTracks.items as List<TrackItem?>?)
        }

        artistsAdapter = ArtistsAdapter {
                artistUrl -> openSpotifyLink(artistUrl)
        }
        binding.basedOnArtistsRecycler.adapter = artistsAdapter
        binding.basedOnArtistsRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.EXTRA_LARGE), Orientation.HORIZONTAL)
        )
        viewModel.shuffledArtists.observe(viewLifecycleOwner) {
                shuffledArtists -> artistsAdapter.submitList(shuffledArtists.items)
        }

        binding.getBasedOnRandomTracksButton.setOnClickListener {
            viewModel.track.observe(viewLifecycleOwner) { track ->
                viewModel.getCurrentUserRecommendations(true)
            }
            recommendedBasedOn = "tracks"
            areRecommendedSongsVisible = true
            discoverUtilsUI.updateUIForTracksButton()
        }

        binding.getBasedOnRandomArtistsButton.setOnClickListener {
            viewModel.artist.observe(viewLifecycleOwner) { artist ->
                viewModel.getCurrentUserRecommendations(false)
            }
            recommendedBasedOn = "artists"
            areRecommendedSongsVisible = true
            discoverUtilsUI.updateUIForArtistsButton()

        }

        binding.discoverTrackImage.setOnClickListener {
            openSpotifyLink(viewModel.discoverTrack.value!!.tracks[0].external_urls.spotify)
        }

        binding.discoverAgainButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            if (recommendedBasedOn == "tracks") {
                viewModel.track.observe(viewLifecycleOwner) {
                    viewModel.getCurrentUserRecommendations(true)
                }
            } else if (recommendedBasedOn == "artists") {
                viewModel.artist.observe(viewLifecycleOwner) {
                    viewModel.getCurrentUserRecommendations(false)
                }
            }
        }

        binding.playSampleButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                stopSongSample()
            } else {
                playSongSample()
            }
        }

        if (savedInstanceState != null) {
            areRecommendedSongsVisible = savedInstanceState.getBoolean("areRecommendedSongsVisible")
            recommendedBasedOn = savedInstanceState.getString("recommendedBasedOn").toString()

            if (areRecommendedSongsVisible) {
                when (recommendedBasedOn) {
                    "tracks" -> {
                        discoverUtilsUI.updateUIForTracksButton()
                    }
                    "artists" -> discoverUtilsUI.updateUIForArtistsButton()
                    "none", "null" -> discoverUtilsUI.updateUIDiscoverAgain()
                    else -> {
                        recommendedBasedOn = "none"
                        discoverUtilsUI.updateUIDiscoverAgain()
                    }
                }
            } else {
                recommendedBasedOn = "none"
                discoverUtilsUI.updateUIDiscoverAgain()
            }
        }

        val backButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (recommendedBasedOn == "tracks" || recommendedBasedOn == "artists") {
                    recommendedBasedOn = "none"
                    stopSongSample()
                    discoverUtilsUI.updateUIDiscoverAgain()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)

        return binding.root
    }

    private fun playSongSample() {
        val previewUrl = viewModel.discoverTrack.value!!.tracks[0].preview_url

        if (previewUrl != null) {
            discoverUtilsUI.updateUiLoadingSample()

            mediaPlayer.apply {
                reset()
                setOnPreparedListener { mp ->
                    discoverUtilsUI.updateUiStopSample()
                    mp.start()
                }
                setOnErrorListener { mp, _, _ ->
                    Log.e("MediaPlayer", "Error occurred during preparation")
                    discoverUtilsUI.updateUiFailedToLoadSample()
                    false
                }
                try {
                    setDataSource(previewUrl)
                    prepareAsync()
                } catch (e: Exception) {
                    discoverUtilsUI.updateUiFailedToLoadSample()
                    Log.e("MediaPlayer", "Error setting data source: ${e.message}")
                }
            }
        }
    }

    private fun stopSongSample() {
        mediaPlayer.stop()
        discoverUtilsUI.updateUiPlaySample()
    }

    private fun openSpotifyLink(spotifyUrl: String) {
        val spotifyUri = Uri.parse(spotifyUrl)
        val profileIntent = Intent(Intent.ACTION_VIEW, spotifyUri)

        if (profileIntent.resolveActivity(requireContext().packageManager) != null) {
            profileIntent.putExtra(Intent.EXTRA_REFERRER, "android-app://${requireContext().packageName}")
            startActivity(profileIntent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
            startActivity(webIntent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("areRecommendedSongsVisible", areRecommendedSongsVisible)
        outState.putString("recommendedBasedOn", recommendedBasedOn)
    }

    override fun onStop() {
        super.onStop()
        stopSongSample()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}