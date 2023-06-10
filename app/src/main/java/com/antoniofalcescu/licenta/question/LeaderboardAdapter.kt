package com.antoniofalcescu.licenta.question

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.databinding.LeaderbordItemViewBinding
import com.antoniofalcescu.licenta.game.GameRoom
import com.antoniofalcescu.licenta.home.User

class LeaderboardAdapter(): ListAdapter<Triple<User?, GameRoom, Question>, LeaderboardAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: LeaderbordItemViewBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User?, gameRoom: GameRoom, question: Question) {
            binding.user = user
            binding.gameRoom = gameRoom
            binding.question = question
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<Triple<User?, GameRoom, Question>>() {
        override fun areItemsTheSame(oldItem: Triple<User?, GameRoom, Question>, newItem: Triple<User?, GameRoom, Question>): Boolean {
            return oldItem.first == newItem.first && oldItem.second == newItem.second && oldItem.third == newItem.third
        }

        override fun areContentsTheSame(oldItem: Triple<User?, GameRoom, Question>, newItem: Triple<User?, GameRoom, Question>): Boolean {
            return oldItem.first == newItem.first && oldItem.second == newItem.second && oldItem.third == newItem.third
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LeaderbordItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position).first
        val gameRoom = getItem(position).second
        val question = getItem(position).third

        holder.bind(user, gameRoom, question)
    }
}