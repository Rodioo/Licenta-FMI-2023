package com.antoniofalcescu.licenta.discover

import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.FragmentDiscoverBinding

class DiscoverUtilsUI(
    private val fragment: DiscoverFragment,
    private val binding: FragmentDiscoverBinding,
    private val viewModel: DiscoverViewModel,
    private val lifecycleOwner: LifecycleOwner,
    ) {

    fun updateUIForTracksButton() {
        updateInitialButtonsVisibility(false)
        updateDiscoverTrackVisibility(true)
        resetSampleUI()

        binding.basedOnTracksView.visibility = View.VISIBLE
        binding.basedOnArtistsView.visibility = View.GONE

    }

    fun updateUIForArtistsButton() {
        updateInitialButtonsVisibility(false)
        updateDiscoverTrackVisibility(true)
        resetSampleUI()

        binding.basedOnTracksView.visibility = View.GONE
        binding.basedOnArtistsView.visibility = View.VISIBLE

    }

    fun updateUIDiscoverAgain() {
        updateInitialButtonsVisibility(true)
        updateDiscoverTrackVisibility(false)
    }

    private fun resetSampleUI() {
        viewModel.discoverTrack.observe(lifecycleOwner) { discoverTrack ->
            if (discoverTrack != null && discoverTrack.tracks.isNotEmpty() && discoverTrack.tracks[0].preview_url != null) {
                updateUiPlaySample()
            } else {
                updateUiNoSample()
            }
        }
    }

    fun updateUiNoSample() {
        Log.e("apel", "noSample")
        binding.playSampleButton.text = fragment.resources.getString(R.string.no_sample)
        binding.playSampleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(fragment.requireContext(), R.drawable.baseline_cancel_24),
            null,
            null,
            null
        )
        binding.playSampleButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(fragment.requireContext(), R.color.grey)
        )
        binding.playSampleButton.isEnabled = false
    }

    fun updateUiPlaySample() {
        binding.playSampleButton.text = fragment.resources.getString(R.string.play_sample)
        binding.playSampleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(fragment.requireContext(), R.drawable.baseline_play_circle_24),
            null,
            null,
            null
        )
        binding.playSampleButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(fragment.requireContext(), R.color.md_theme_dark_primary)
        )
        binding.playSampleButton.isEnabled = true
    }

    fun updateUiStopSample() {
        binding.playSampleButton.text = fragment.resources.getString(R.string.stop_sample)
        binding.playSampleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(fragment.requireContext(), R.drawable.baseline_pause_circle_24),
            null,
            null,
            null
        )
        binding.playSampleButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(fragment.requireContext(), R.color.md_theme_dark_primary)
        )
        binding.playSampleButton.isEnabled = true
    }

    fun updateUiLoadingSample() {
        binding.playSampleButton.text = fragment.resources.getString(R.string.loading)
        binding.playSampleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(fragment.requireContext(), R.drawable.loading_animation),
            null,
            null,
            null
        )
        binding.playSampleButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(fragment.requireContext(), R.color.grey)
        )
        binding.playSampleButton.isEnabled = false
    }

    fun updateUiFailedToLoadSample() {
        binding.playSampleButton.text = fragment.resources.getString(R.string.failed_to_load)
        binding.playSampleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(fragment.requireContext(), R.drawable.baseline_cancel_24),
            null,
            null,
            null
        )
        binding.playSampleButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(fragment.requireContext(), R.color.grey)
        )
        binding.playSampleButton.isEnabled = false
    }

    private fun updateInitialButtonsVisibility(isVisible: Boolean) {
        binding.discoverRecommendationText.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.discoverButtonsView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun updateDiscoverTrackVisibility(isVisible: Boolean) {
        binding.thinkYouLikeText.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.discoveredTrackLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.discoverAgainButton.visibility = if (isVisible) View.VISIBLE else View.GONE

        if (!isVisible) {
            binding.basedOnTracksView.visibility = View.GONE
            binding.basedOnArtistsView.visibility = View.GONE
        }
    }
}

