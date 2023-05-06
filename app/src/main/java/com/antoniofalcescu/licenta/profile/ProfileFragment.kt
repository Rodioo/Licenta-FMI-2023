package com.antoniofalcescu.licenta.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.databinding.FragmentProfileBinding
import com.antoniofalcescu.licenta.profile.artists.ArtistsAdapter
import com.antoniofalcescu.licenta.profile.recentlyPlayedTracks.RecentlyPlayedAdapter
import com.antoniofalcescu.licenta.profile.tracks.TracksAdapter
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var artistsAdapter: ArtistsAdapter
    private lateinit var recentlyPlayedAdapter: RecentlyPlayedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)

        viewModelFactory = ProfileViewModelFactory(
            ProfileFragmentArgs.fromBundle(requireArguments()).accessToken
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        tracksAdapter = TracksAdapter(viewModel) {
            trackUrl -> openSpotifyLink(trackUrl)
        }
        binding.topTracksRecycler.adapter = tracksAdapter
        binding.topTracksRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.SMALL), Orientation.HORIZONTAL)
        )
        viewModel.track.observe(viewLifecycleOwner) {
                track -> tracksAdapter.submitList(track.items)
        }

        artistsAdapter = ArtistsAdapter(viewModel) {
            artistUrl -> openSpotifyLink(artistUrl)
        }
        binding.topArtistsRecycler.adapter = artistsAdapter
        binding.topArtistsRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.SMALL), Orientation.HORIZONTAL)
        )
        viewModel.artist.observe(viewLifecycleOwner) {
                artist -> artistsAdapter.submitList(artist.items)
        }

        recentlyPlayedAdapter = RecentlyPlayedAdapter(viewModel) {
                trackUrl -> openSpotifyLink(trackUrl)
        }
        binding.recentlyPlayedRecycler.adapter = recentlyPlayedAdapter
        binding.recentlyPlayedRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.SMALL), Orientation.VERTICAL)
        )
        viewModel.recentlyPlayed.observe(viewLifecycleOwner) {
                recentlyPlayed -> recentlyPlayedAdapter.submitList(recentlyPlayed.items)
        }

        binding.openSpotifyButton.setOnClickListener {
            openSpotifyLink(viewModel.profile.value!!.external_urls.spotify)
        }

        binding.currentlyPlayingImage.setOnClickListener {
            openSpotifyLink(viewModel.currentTrack.value!!.item!!.external_urls.spotify)
        }

        updateUI()

        return binding.root
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

    private fun updateUI() {
        fun updateProfileVisibility(isVisible: Boolean) {
            binding.profileView.visibility = if(isVisible) View.VISIBLE else View.GONE
        }

        fun updateLoadingVisibility(isVisible: Boolean) {
            binding.profileLoading.visibility = if(isVisible) View.VISIBLE else View.GONE
            binding.separator.visibility = if(isVisible) View.GONE else View.VISIBLE
            binding.scrollView.visibility = if(isVisible) View.GONE else View.VISIBLE

        }

        fun updateCurrentlyPlayingVisibility(isVisible: Boolean) {
            binding.currentlyPlayingText.visibility = if(isVisible) View.VISIBLE else View.GONE
            binding.currentTrackView.visibility = if(isVisible) View.VISIBLE else View.GONE
        }

        fun updateTopTracksVisibility(isVisible: Boolean) {
            binding.topTracksText.visibility = if(isVisible) View.VISIBLE else View.GONE
            binding.topTracksRecycler.visibility = if(isVisible) View.VISIBLE else View.GONE
        }

        fun updateTopArtistsVisibility(isVisible: Boolean) {
            binding.topArtistsText.visibility = if(isVisible) View.VISIBLE else View.GONE
            binding.topArtistsRecycler.visibility = if(isVisible) View.VISIBLE else View.GONE
        }

        fun updateRecentlyPlayedTracksVisibility(isVisible: Boolean) {
            binding.recentlyPlayedText.visibility = if(isVisible) View.VISIBLE else View.GONE
            binding.recentlyPlayedRecycler.visibility = if(isVisible) View.VISIBLE else View.GONE
        }

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            updateProfileVisibility(profile != null)
            updateLoadingVisibility(profile == null)
        }

        viewModel.currentTrack.observe(viewLifecycleOwner) { currentTrack ->
            if (currentTrack == null || currentTrack.emptyResponse != null) {
                updateCurrentlyPlayingVisibility(false)
            } else {
                updateCurrentlyPlayingVisibility(true)
            }
        }

        viewModel.track.observe(viewLifecycleOwner) { track ->
            updateTopTracksVisibility(track != null)
        }

        viewModel.artist.observe(viewLifecycleOwner) { artist ->
            updateTopArtistsVisibility(artist != null)
        }

        viewModel.recentlyPlayed.observe(viewLifecycleOwner) { recentTracks ->
            updateRecentlyPlayedTracksVisibility(recentTracks != null)
        }
    }
}