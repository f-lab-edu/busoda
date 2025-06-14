package com.chaeny.busoda.stopdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                composeBusHeader.setContent {
                    MaterialTheme {
                        BusInfoHeader(busData.busNumber, busData.nextStopName)
                    }
                }
            }
            bindArrivalInfos(busData, binding, timerFlow)
        }

        private fun bindArrivalInfos(busData: BusInfo, binding: ListItemBusBinding, timerFlow: Flow<Int>) {
            with(binding) {
                firstArrivalInfoView.bindArrivalInfo(busData.arrivalInfos.getOrNull(0), 0, timerFlow)
                secondArrivalInfoView.bindArrivalInfo(busData.arrivalInfos.getOrNull(1), 1, timerFlow)
            }
        }

        @Composable
        fun BusInfoHeader(
            busNumber: String,
            nextStopName: String,
            modifier: Modifier = Modifier
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = busNumber,
                    modifier = Modifier.weight(0.3f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = nextStopName,
                    modifier = Modifier
                        .weight(0.55f)
                        .padding(end = 5.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.way),
                    style = MaterialTheme.typography.bodyLarge
                )
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
