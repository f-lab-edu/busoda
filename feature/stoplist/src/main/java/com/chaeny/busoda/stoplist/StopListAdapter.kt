package com.chaeny.busoda.stoplist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.findNavController
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

        init {
            binding.root.setOnClickListener {
                navigateToStopDetail(binding.textStopName.text.toString())
            }
        }

        private fun navigateToStopDetail(stopName: String) {
            val uri = "android-app://com.chaeny.busoda/fragment_stop_detail?stopName=$stopName"
            val request = NavDeepLinkRequest.Builder
                .fromUri(uri.toUri())
                .build()
            binding.root.findNavController().navigate(request)
        }

        private fun BusStop.formatNextStopName(): String {
            return binding.root.context.getString(R.string.direction, nextStopName)
        }

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
