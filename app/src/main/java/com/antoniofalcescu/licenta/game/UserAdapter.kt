package com.antoniofalcescu.licenta.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.UserItemViewBinding
import com.antoniofalcescu.licenta.home.User

class UserAdapter(
    private val currentUser: User,
    private val numberOfPlayers: Int = 4,
    private val onItemClick: (String) -> Unit
): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var userList: List<User?>? = listOf()

    class ViewHolder(
        private var binding: UserItemViewBinding,
        ): RecyclerView.ViewHolder(binding.root) {
        val userView = binding.userView

        fun bind(user: User, currentUserIsOwner: Boolean, showOwnKickButton: Boolean) {
            if (currentUserIsOwner || showOwnKickButton) {
                binding.kickUserButton.visibility = View.VISIBLE
            } else {
                binding.kickUserButton.visibility = View.GONE
            }
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
        var currentUserIsOwner = false
        var showOwnKickButton = false
        if (!userList.isNullOrEmpty()) {
            user = if (position < userList!!.size) userList!![position] else null
            if (currentUser.id_spotify == userList!![0]?.id_spotify) {
                currentUserIsOwner = true
            }
            if (currentUser.id_spotify == user?.id_spotify) {
                showOwnKickButton = true
            }
        }

        if (user != null) {
            holder.bind(user, currentUserIsOwner, showOwnKickButton)
        } else {
            holder.bindEmptyUser()
        }

        holder.userView.setOnClickListener {
            user?.let { onItemClick(it.toString()) }
        }
    }

    fun submitList(users: List<User?>?) {
        userList = users
        notifyDataSetChanged()
    }
}