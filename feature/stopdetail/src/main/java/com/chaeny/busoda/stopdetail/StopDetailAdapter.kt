package com.chaeny.busoda.stopdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.stopdetail.databinding.ListItemBusBinding
import kotlinx.coroutines.flow.Flow

internal class StopDetailAdapter(private val timerFlow: Flow<Int>) :
    ListAdapter<BusInfo, StopDetailAdapter.BusDetailViewHolder>(BusDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusDetailViewHolder {
        return BusDetailViewHolder(
            ListItemBusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), timerFlow
        )
    }

    override fun onBindViewHolder(holder: BusDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BusDetailViewHolder(
        private val binding: ListItemBusBinding,
        private val timerFlow: Flow<Int>
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(busData: BusInfo) {
            with(binding) {
                textBusNumber.text = busData.busNumber
                textNextStop.text = busData.nextStopName
            }
            bindArrivalInfos(busData, binding, timerFlow)
        }

        private fun bindArrivalInfos(busData: BusInfo, binding: ListItemBusBinding, timerFlow: Flow<Int>) {
            with(binding) {
                firstArrivalInfoView.bindArrivalInfo(busData.arrivalInfos.getOrNull(0), 0, timerFlow)
                secondArrivalInfoView.bindArrivalInfo(busData.arrivalInfos.getOrNull(1), 1, timerFlow)
            }
        }
    }
}

private class BusDetailDiffCallback : DiffUtil.ItemCallback<BusInfo>() {
    override fun areItemsTheSame(oldItem: BusInfo, newItem: BusInfo): Boolean {
        return oldItem.busNumber == newItem.busNumber
    }

    override fun areContentsTheSame(oldItem: BusInfo, newItem: BusInfo): Boolean {
        return oldItem == newItem
    }
}
