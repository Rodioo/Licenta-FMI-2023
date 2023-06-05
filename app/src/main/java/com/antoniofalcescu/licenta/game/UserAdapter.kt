package com.antoniofalcescu.licenta.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.UserItemViewBinding
import com.antoniofalcescu.licenta.home.User

class UserAdapter(
    private val numberOfPlayers: Int = 4,
    private val onItemClick: (String) -> Unit
): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var userList: List<User?>? = listOf()

    class ViewHolder(
        private var binding: UserItemViewBinding
        ): RecyclerView.ViewHolder(binding.root) {
        val userView = binding.userView

        fun bind(user: User) {
            binding.kickUserButton.visibility = View.VISIBLE
            binding.user = user
            binding.executePendingBindings()
        }

        fun bindEmptyUser() {
            binding.kickUserButton.visibility = View.GONE
            binding.profileImage.setImageResource(R.drawable.baseline_person_add_alt_1_24)
            binding.user = null
            binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return numberOfPlayers
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var user: User? = null
        if (userList != null) {
            user = if (position < userList!!.size) userList!![position] else null
        }

        if (user != null) {
            holder.bind(user)
        } else {
            holder.bindEmptyUser()
        }

        holder.userView.setOnClickListener {
            user?.let { onItemClick(it.name) }
        }
    }

    fun submitList(users: List<User?>?) {
        userList = users
        notifyDataSetChanged()
    }
}