package com.chaeny.busoda.stoplist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chaeny.busoda.stoplist.databinding.ListItemStopBinding

internal class StopListAdapter : ListAdapter<List<String>, StopListAdapter.BusStopViewHolder>(BusStopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusStopViewHolder {
        return BusStopViewHolder(
            ListItemStopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BusStopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BusStopViewHolder(private val binding: ListItemStopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stopData: List<String>) {
            with(binding) {
                textStopId.text = stopData[0]
                textStopName.text = stopData[1]
                textNextStop.text = stopData[2]
            }
        }
    }
}

private class BusStopDiffCallback : DiffUtil.ItemCallback<List<String>>() {

    override fun areItemsTheSame(oldItem: List<String>, newItem: List<String>): Boolean {
        return oldItem[0] == newItem[0]
    }

    override fun areContentsTheSame(oldItem: List<String>, newItem: List<String>): Boolean {
        return oldItem == newItem
    }
}
