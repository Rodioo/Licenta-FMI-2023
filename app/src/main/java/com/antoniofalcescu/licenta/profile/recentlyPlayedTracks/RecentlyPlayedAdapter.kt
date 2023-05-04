package com.antoniofalcescu.licenta.profile.recentlyPlayedTracks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.databinding.RecentlyPlayedItemViewBinding
import com.antoniofalcescu.licenta.profile.ProfileViewModel
import com.antoniofalcescu.licenta.profile.tracks.TrackItem

class RecentlyPlayedAdapter(
    private val viewModel: ProfileViewModel,
    private val onItemClick: (String) -> Unit
): ListAdapter<RecentlyPlayedTrackItem, RecentlyPlayedAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: RecentlyPlayedItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        val recentlyPlayedView = binding.recentlyPlayedView

        fun bind(recentlyPlayedTrackItem: RecentlyPlayedTrackItem) {
            binding.recentlyPlayedTrackItem = recentlyPlayedTrackItem
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<RecentlyPlayedTrackItem>() {
        override fun areItemsTheSame(oldItem: RecentlyPlayedTrackItem, newItem: RecentlyPlayedTrackItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RecentlyPlayedTrackItem, newItem: RecentlyPlayedTrackItem): Boolean {
            return oldItem.track.id == newItem.track.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecentlyPlayedItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recentlyPlayedTrackItem = getItem(position)

        holder.bind(recentlyPlayedTrackItem)

        holder.recentlyPlayedView.setOnClickListener {
            onItemClick(recentlyPlayedTrackItem.track.external_urls.spotify)
        }
    }
}