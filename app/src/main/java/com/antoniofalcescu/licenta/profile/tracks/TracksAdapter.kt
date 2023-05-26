package com.antoniofalcescu.licenta.profile.tracks

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.databinding.TrackItemViewBinding
import com.antoniofalcescu.licenta.profile.ProfileViewModel

class TracksAdapter(
    private val onItemClick: (String) -> Unit
    ): ListAdapter<TrackItem?, TracksAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: TrackItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        val trackView = binding.trackView

        fun bind(trackItem: TrackItem) {
            binding.trackItem = trackItem
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<TrackItem?>() {
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TrackItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trackItem = getItem(position)

        if (trackItem != null) {
            holder.bind(trackItem)
        }

        holder.trackView.setOnClickListener {
            if (trackItem != null) {
                onItemClick(trackItem.external_urls.spotify)
            }
        }
    }
}