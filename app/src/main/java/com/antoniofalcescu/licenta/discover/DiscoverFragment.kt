package com.antoniofalcescu.licenta.discover

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.FragmentDiscoverBinding
import com.antoniofalcescu.licenta.profile.artists.ArtistsAdapter
import com.antoniofalcescu.licenta.profile.tracks.TracksAdapter
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing

//TODO: Find pause icon/X icon and adjust sample button accordingly (also see what's up with the double sound bug)
class DiscoverFragment : Fragment() {

    private lateinit var binding: FragmentDiscoverBinding
    private lateinit var viewModel: DiscoverViewModel
    private lateinit var viewModelFactory: DiscoverViewModelFactory

    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var artistsAdapter: ArtistsAdapter

    private var areRecommendedSongsVisible: Boolean = false
    private var recommendedBasedOn: String = "none"

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDiscoverBinding.inflate(inflater)

        if (savedInstanceState != null) {
            areRecommendedSongsVisible = savedInstanceState.getBoolean("areRecommendedSongsVisible")
            recommendedBasedOn = savedInstanceState.getString("recommendedBasedOn").toString()

            if (areRecommendedSongsVisible) {
                when (recommendedBasedOn) {
                    "tracks" -> updateUIForTracksButton()
                    "artists" -> updateUIForArtistsButton()
                    "none", "null" -> updateUIDiscoverAgain()
                    else -> updateUIDiscoverAgain()
                }
            } else {
                updateUIDiscoverAgain()
            }
        }

        viewModelFactory = DiscoverViewModelFactory(this.requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[DiscoverViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        tracksAdapter = TracksAdapter {
                trackUrl -> openSpotifyLink(trackUrl)
        }
        binding.basedOnTracksRecycler.adapter = tracksAdapter
        binding.basedOnTracksRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.EXTRA_LARGE), Orientation.HORIZONTAL)
        )
        viewModel.shuffledTracks.observe(viewLifecycleOwner) {
                shuffledTracks -> tracksAdapter.submitList(shuffledTracks.items)
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
            updateUIForTracksButton()
        }

        binding.getBasedOnRandomArtistsButton.setOnClickListener {
            viewModel.artist.observe(viewLifecycleOwner) { artist ->
                viewModel.getCurrentUserRecommendations(false)
            }

            updateUIForArtistsButton()

        }

        binding.discoverTrackImage.setOnClickListener {
            viewModel.discoverTrack.observe(viewLifecycleOwner) { discoverTrack ->
                openSpotifyLink(discoverTrack.tracks[0].external_urls.spotify)
            }
        }

        binding.discoverAgainButton.setOnClickListener {
            updateUIDiscoverAgain()
        }

        binding.playSampleButton.setOnClickListener {
            if (binding.playSampleButton.text == "Play Sample") {
                playSongSample()
            } else {
                stopSongSample()
            }
        }

        return binding.root
    }

    private fun playSongSample() {
        viewModel.discoverTrack.observe(viewLifecycleOwner) { discoverTrack ->
            if (discoverTrack.tracks[0].preview_url != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(discoverTrack.tracks[0].preview_url)
                    prepare()
                    start()
                }
                binding.playSampleButton.text = "Stop Sample"
            }
        }
    }

    private fun stopSongSample() {
        mediaPlayer.stop()
        binding.playSampleButton.text = "Play Sample"
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

    private fun updateUIForTracksButton() {
        recommendedBasedOn = "tracks"

        updateInitialButtonsVisibility(false)
        updateDiscoverTrackVisibility(true)

        binding.basedOnTracksView.visibility = View.VISIBLE
        binding.basedOnArtistsView.visibility = View.GONE

    }

    private fun updateUIForArtistsButton() {
        recommendedBasedOn = "artists"

        updateInitialButtonsVisibility(false)
        updateDiscoverTrackVisibility(true)

        binding.basedOnTracksView.visibility = View.GONE
        binding.basedOnArtistsView.visibility = View.VISIBLE

    }

    private fun updateUIDiscoverAgain() {
        recommendedBasedOn = "none"

        updateInitialButtonsVisibility(true)
        updateDiscoverTrackVisibility(false)
    }

    private fun updateInitialButtonsVisibility(isVisible: Boolean) {
        binding.discoverRecommendationText.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.discoverButtonsView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun updateDiscoverTrackVisibility(isVisible: Boolean) {
        areRecommendedSongsVisible = isVisible

        binding.thinkYouLikeText.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.discoveredTrackLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.discoverAgainButton.visibility = if (isVisible) View.VISIBLE else View.GONE

        if (!isVisible) {
            binding.basedOnTracksView.visibility = View.GONE
            binding.basedOnArtistsView.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("areRecommendedSongsVisible", areRecommendedSongsVisible)
        outState.putString("recommendedBasedOn", recommendedBasedOn)
    }
}