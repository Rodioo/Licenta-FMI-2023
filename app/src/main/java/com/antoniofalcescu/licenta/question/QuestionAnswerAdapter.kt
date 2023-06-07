package com.antoniofalcescu.licenta.question

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.databinding.QuestionAnswerItemViewBinding

class QuestionAnswerAdapter(
    private val onItemClick: (String) -> Unit
): ListAdapter<String, QuestionAnswerAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: QuestionAnswerItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        val answerButton = binding.answerButton

        fun bind(answer: String) {
            binding.answer = answer
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(QuestionAnswerItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val answer = getItem(position)

        holder.bind(answer)

        holder.answerButton.setOnClickListener {
            Log.e("answered", answer)
        }
    }
}