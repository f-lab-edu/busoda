package com.chaeny.busoda.stopdetail

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chaeny.busoda.stopdetail.databinding.FragmentStopDetailBinding
import com.chaeny.busoda.ui.event.EventObserver
import com.chaeny.busoda.ui.theme.DarkGreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StopDetailFragment : Fragment() {

    private lateinit var binding: FragmentStopDetailBinding
    private val viewModel: StopDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopDetailBinding.inflate(inflater, container, false)
        val adapter = StopDetailAdapter(viewModel.timer)
        binding.busList.adapter = adapter
        subscribeUi(adapter)
        subscribeRefreshEvent()
        return binding.root
    }

    private fun subscribeUi(adapter: StopDetailAdapter) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.busListLoadingBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.stopDetail.observe(viewLifecycleOwner) { stopDetail ->
            displayStopId()
            displayStopName(stopDetail.stopName)
            displayBusEmoji()
            displayRefreshButton()
            adapter.submitList(stopDetail.busInfos)
        }
    }

    private fun displayStopId() {
        binding.composeBusStopId.setContent {
            val stopId by viewModel.stopId.observeAsState()
            MaterialTheme {
                StopId(stopId!!)
            }
        }
    }

    private fun displayStopName(stopName: String) {
        binding.composeBusStopName.setContent {
            MaterialTheme {
                StopName(stopName)
            }
        }
    }

//    private fun subscribeCountdownTimer() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.timer.collect { countdownValue ->
//                    updateBusAnimation(countdownValue)
//                }
//            }
//        }
//    }

//    private fun updateBusAnimation(countdownValue: Int) {
//        val maxCount = 15
//        val moveStep = maxCount - countdownValue
//        val totalDistance = binding.textStopEmoji.left - binding.textBusEmoji.left
//        val stepDistance = (totalDistance / maxCount).toFloat()
//        val translationValue = stepDistance * moveStep
//
//        ObjectAnimator.ofFloat(binding.textBusEmoji, "translationX", translationValue).apply {
//            duration = 1000
//            start()
//        }
//    }

    private fun displayBusEmoji() {
        binding.composeBusEmoji.setContent {
            MaterialTheme {
                Row {
                    BusEmoji()
                    StopEmoji()
                }
            }
        }
    }

    private fun subscribeRefreshEvent() {
        viewModel.refreshEvent.observe(viewLifecycleOwner, EventObserver { isRefresh ->
            if (isRefresh) {
                startRotateAnimation(binding.composeRefreshButton)
            }
        })
    }

    private fun displayRefreshButton() {
        binding.composeRefreshButton.setContent {
            MaterialTheme {
                RefreshButton(
                    onClick = { viewModel.refreshData() }
                )
            }
        }
    }

    private fun startRotateAnimation(view: View) {
        ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 180f).apply {
            duration = 500
            start()
        }
    }

    @Composable
    fun StopId(
        stopId: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = stopId,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    @Composable
    fun StopName(
        stopName: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = stopName.ifEmpty { stringResource(R.string.no_info) },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
    }

    @Composable
    fun BusEmoji(
        modifier: Modifier = Modifier
    ) {
        Text(
            text = stringResource(R.string.bus_emoji),
            modifier = modifier
                .padding(start = 35.dp)
                .graphicsLayer(rotationY = 180f),
            fontSize = with(LocalDensity.current) { 30.dp.toSp() }
        )
    }

    @Composable
    fun StopEmoji(
        modifier: Modifier = Modifier
    ) {
        Text(
            text = stringResource(R.string.stop_emoji),
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 30.dp),
            fontSize = with(LocalDensity.current) { 30.dp.toSp() },
            textAlign = TextAlign.End
        )
    }

    @Composable
    fun RefreshButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = DarkGreen,
            shape = CircleShape,
            modifier = modifier.padding(25.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.refresh),
                tint = Color.Black
            )
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

    @Preview(showBackground = true)
    @Composable
    fun StopIdPreview() {
        MaterialTheme { StopId("16206") }
    }

    @Preview(showBackground = true)
    @Composable
    fun StopNamePreview() {
        MaterialTheme { StopName("화곡역4번출구") }
    }

    @Preview(showBackground = true)
    @Composable
    fun StopEmojiPreview() {
        MaterialTheme {
            Row {
                BusEmoji()
                StopEmoji()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun RefreshButtonPreview() {
        MaterialTheme {
            RefreshButton(onClick = {})
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun BusInfoHeaderPreview() {
        MaterialTheme {
            BusInfoHeader(
                busNumber = "심야A21",
                nextStopName = "강서구청사거리.서울디지털대학교"
            )
        }
    }

}
