package com.antoniofalcescu.licenta.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.databinding.GenreItemViewBinding

class GenreAdapter(
    private val onItemClick: (String) -> Unit
): ListAdapter<GenreItem, GenreAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: GenreItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        val genreView = binding.genreView

        fun bind(genreItem: GenreItem) {
            binding.genreItem = genreItem
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<GenreItem>() {
        override fun areItemsTheSame(oldItem: GenreItem, newItem: GenreItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: GenreItem, newItem: GenreItem): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(GenreItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genreItem = getItem(position)

        holder.bind(genreItem)

        holder.genreView.setOnClickListener {
            onItemClick(genreItem.name)
        }
    }
}