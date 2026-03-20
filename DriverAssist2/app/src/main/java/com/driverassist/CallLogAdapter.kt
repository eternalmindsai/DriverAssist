package com.driverassist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.driverassist.databinding.ItemCallLogBinding

class CallLogAdapter : ListAdapter<CallLogEntry, CallLogAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(val binding: ItemCallLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        holder.binding.tvPhoneNumber.text = entry.phoneNumber
        holder.binding.tvTimestamp.text = entry.formattedTime
        holder.binding.tvSmsNote.text = "✉ Auto-SMS sent"
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CallLogEntry>() {
            override fun areItemsTheSame(oldItem: CallLogEntry, newItem: CallLogEntry) =
                oldItem.timestamp == newItem.timestamp
            override fun areContentsTheSame(oldItem: CallLogEntry, newItem: CallLogEntry) =
                oldItem == newItem
        }
    }
}
