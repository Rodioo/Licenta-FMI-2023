package com.antoniofalcescu.licenta.question

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.databinding.QuestionAnswerItemViewBinding

class QuestionAnswerAdapter(
    private val onItemClick: (String) -> Unit
): ListAdapter<Pair<String, String>, QuestionAnswerAdapter.ViewHolder>(DiffCallback) {

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    class ViewHolder(private var binding: QuestionAnswerItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        val answerButton = binding.answerButton
        val profileImage = binding.profileImage

        fun bind(answer: String, profile_url: String) {
            binding.answer = answer
            binding.profileUrl = profile_url
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.first == newItem.first && oldItem.second == newItem.second
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.first == newItem.first && oldItem.second == newItem.second
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(QuestionAnswerItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val answer = getItem(position).first
        val profile_url = getItem(position).second

        holder.bind(answer, profile_url)

        holder.answerButton.setOnClickListener {
            recyclerView?.let { recyclerView ->
                for (i in 0 until recyclerView.childCount) {
                    val itemHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i)) as? ViewHolder
                    itemHolder?.profileImage?.visibility = View.GONE
                }
            }

            holder.profileImage.visibility = View.VISIBLE

            onItemClick(answer)
        }
    }
}