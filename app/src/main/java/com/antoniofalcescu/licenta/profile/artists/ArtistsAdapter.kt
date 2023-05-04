package com.antoniofalcescu.licenta.profile.artists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.databinding.ArtistItemViewBinding
import com.antoniofalcescu.licenta.profile.ProfileViewModel

class ArtistsAdapter(
    private val viewModel: ProfileViewModel,
    private val onItemClick: (String) -> Unit
): ListAdapter<ArtistItem, ArtistsAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: ArtistItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        val artistView = binding.artistView

        fun bind(artistItem: ArtistItem) {
            binding.artistItem = artistItem
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<ArtistItem>() {
        override fun areItemsTheSame(oldItem: ArtistItem, newItem: ArtistItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ArtistItem, newItem: ArtistItem): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ArtistItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artistItem = getItem(position)

        holder.bind(artistItem)

        holder.artistView.setOnClickListener {
            onItemClick(artistItem.external_urls.spotify)
        }
    }
}