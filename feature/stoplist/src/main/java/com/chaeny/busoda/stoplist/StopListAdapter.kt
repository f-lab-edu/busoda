package com.chaeny.busoda.stoplist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chaeny.busoda.stoplist.databinding.ListItemStopBinding

internal class StopListAdapter : ListAdapter<BusStop, StopListAdapter.BusStopViewHolder>(BusStopDiffCallback()) {

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
        fun bind(stopData: BusStop) {
            with(binding) {
                textStopId.text = stopData.stopId
                textStopName.text = stopData.stopName
                textNextStop.text = stopData.formatNextStopName()
            }
        }
    }
}

private class BusStopDiffCallback : DiffUtil.ItemCallback<BusStop>() {

    override fun areItemsTheSame(oldItem: BusStop, newItem: BusStop): Boolean {
        return oldItem.stopId == newItem.stopId
    }

    override fun areContentsTheSame(oldItem: BusStop, newItem: BusStop): Boolean {
        return oldItem == newItem
    }
}
