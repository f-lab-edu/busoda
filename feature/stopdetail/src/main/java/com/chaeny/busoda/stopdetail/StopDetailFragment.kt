package com.chaeny.busoda.stopdetail

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.CongestionLevel
import com.chaeny.busoda.stopdetail.databinding.FragmentStopDetailBinding
import com.chaeny.busoda.ui.event.EventObserver
import com.chaeny.busoda.ui.theme.DarkGreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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

    @Composable
    fun ArrivalTimeText(
        arrivalTime: Long?,
        timerFlow: Flow<Int>,
        modifier: Modifier = Modifier
    ) {
        var displayTime by rememberSaveable { mutableStateOf("") }

        LaunchedEffect(arrivalTime, timerFlow) {
            if (arrivalTime != null) {
                timerFlow.collect {
                    displayTime = setTextRemainingTime(arrivalTime)
                }
            }
        }

        Text(
            text = displayTime,
            modifier = modifier,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleSmall
        )
    }

    @Composable
    fun ArrivalInfo(
        arrivalInfo: BusArrivalInfo?,
        position: Int,
        timerFlow: Flow<Int>,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.nth_bus, position + 1),
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodyMedium
            )
            ArrivalTimeText(
                arrivalTime = arrivalInfo?.arrivalTime,
                timerFlow = timerFlow,
                modifier = Modifier.weight(2.5f)
            )
            Text(
                text = arrivalInfo?.position ?: stringResource(R.string.no_data),
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = arrivalInfo?.getCongestionText() ?: stringResource(R.string.no_data),
                color = arrivalInfo?.getCongestionColor() ?: colorResource(R.color.congestion_unknown),
                modifier = Modifier.weight(1.5f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    @Composable
    fun BusArrivalInfo.getCongestionText(): String {
        return when (congestion) {
            CongestionLevel.VERY_HIGH -> stringResource(R.string.congestion_very_high)
            CongestionLevel.HIGH -> stringResource(R.string.congestion_high)
            CongestionLevel.MEDIUM -> stringResource(R.string.congestion_medium)
            CongestionLevel.LOW -> stringResource(R.string.congestion_low)
            else -> stringResource(R.string.no_data)
        }
    }

    @Composable
    fun BusArrivalInfo.getCongestionColor(): Color {
        return when (congestion) {
            CongestionLevel.VERY_HIGH -> colorResource(R.color.congestion_very_high)
            CongestionLevel.HIGH      -> colorResource(R.color.congestion_high)
            CongestionLevel.MEDIUM    -> colorResource(R.color.congestion_medium)
            CongestionLevel.LOW       -> colorResource(R.color.congestion_low)
            else                      -> colorResource(R.color.congestion_unknown)
        }
    }

    private fun setTextRemainingTime(arrivalTime: Long): String {
        val now = System.currentTimeMillis() / 1000
        val remainingTime = arrivalTime - now
        return formattedArrivalTime(remainingTime)
    }

    private fun formattedArrivalTime(arrivalTime: Long): String {
        if (arrivalTime <= 0) return requireContext().getString(R.string.no_data)

        val minutes = arrivalTime / 60
        val seconds = arrivalTime % 60

        return when {
            minutes > 0 && seconds > 0 -> requireContext().getString(R.string.minutes_seconds, minutes, seconds)
            minutes > 0 -> requireContext().getString(R.string.minutes, minutes)
            else -> requireContext().getString(R.string.seconds, seconds)
        }
    }

    @Composable
    fun BusItem(
        busInfo: BusInfo,
        timerFlow: Flow<Int>,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(bottom = 15.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            BusInfoHeader(
                busNumber = busInfo.busNumber,
                nextStopName = busInfo.nextStopName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 15.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ArrivalInfo(
                arrivalInfo = busInfo.arrivalInfos.getOrNull(0),
                position = 0,
                timerFlow = timerFlow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ArrivalInfo(
                arrivalInfo = busInfo.arrivalInfos.getOrNull(1),
                position = 1,
                timerFlow = timerFlow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 15.dp)
            )
        }
    }

    @Composable
    fun BusList(
        busInfos: List<BusInfo>,
        timerFlow: Flow<Int>,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(modifier) {
            items(busInfos) { busInfo ->
                BusItem(busInfo, timerFlow)
            }
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

    @Preview(showBackground = true)
    @Composable
    fun ArrivalInfoPreview() {
        MaterialTheme {
            ArrivalInfo(arrivalInfo = dummyArrivalInfo, position = 0, timerFlow = flowOf(0))
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun BusItemPreview() {
        MaterialTheme {
            BusItem(busInfo = dummyBusInfo, timerFlow = flowOf(0))
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun BusListPreview() {
        MaterialTheme {
            BusList(busInfos = dummyBusInfos, timerFlow = flowOf(0))
        }
    }

    private val now = System.currentTimeMillis() / 1000
    private val dummyArrivalInfo = BusArrivalInfo(now + 158, "2번째 전", CongestionLevel.MEDIUM)
    private val dummyBusInfo = BusInfo(
        "5712", "등촌중학교.백석초등학교", listOf(
            BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
            BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
        )
    )
    private val dummyBusInfos = listOf(
        BusInfo(
            "604", "화곡본동시장", listOf(
                BusArrivalInfo(now + 158, "2번째 전", CongestionLevel.MEDIUM),
                BusArrivalInfo(now + 978, "9번째 전", CongestionLevel.HIGH)
            )
        ),
        BusInfo(
            "5712", "화곡본동시장", listOf(
                BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
                BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
            )
        ),
        BusInfo(
            "652", "화곡역1번출구", listOf(
                BusArrivalInfo(now + 298, "4번째 전", CongestionLevel.MEDIUM),
                BusArrivalInfo(now + 1100, "11번째 전", CongestionLevel.VERY_HIGH)
            )
        )
    )

}
