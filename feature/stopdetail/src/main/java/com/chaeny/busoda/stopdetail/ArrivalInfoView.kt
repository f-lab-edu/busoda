package com.chaeny.busoda.stopdetail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.CongestionLevel
import com.chaeny.busoda.stopdetail.databinding.ArrivalInfoViewBinding
import kotlinx.coroutines.flow.Flow

class ArrivalInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val layoutInflater = LayoutInflater.from(context)
    private var binding = ArrivalInfoViewBinding.inflate(layoutInflater, this)

    internal fun bindArrivalInfo(arrivalInfo: BusArrivalInfo?, position: Int, timerFlow: Flow<Int>) {
        binding.composeArrivalInfo.setContent {
            MaterialTheme {
                ArrivalInfo(arrivalInfo, position, timerFlow)
            }
        }
    }

    private fun BusArrivalInfo.getCongestionText(): String {
        return when (this.congestion) {
            CongestionLevel.VERY_HIGH -> context.getString(R.string.congestion_very_high)
            CongestionLevel.HIGH -> context.getString(R.string.congestion_high)
            CongestionLevel.MEDIUM -> context.getString(R.string.congestion_medium)
            CongestionLevel.LOW -> context.getString(R.string.congestion_low)
            else -> context.getString(R.string.no_data)
        }
    }

    private fun BusArrivalInfo.getCongestionColor(): Int {
        return when (this.getCongestionText()) {
            context.getString(R.string.congestion_very_high) -> context.getColor(R.color.congestion_very_high)
            context.getString(R.string.congestion_high) -> context.getColor(R.color.congestion_high)
            context.getString(R.string.congestion_medium) -> context.getColor(R.color.congestion_medium)
            context.getString(R.string.congestion_low) -> context.getColor(R.color.congestion_low)
            else -> context.getColor(R.color.congestion_unknown)
        }
    }

    private fun setTextRemainingTime(arrivalTime: Long): String {
        val now = System.currentTimeMillis() / 1000
        val remainingTime = arrivalTime - now
        return formattedArrivalTime(remainingTime)
    }

    private fun formattedArrivalTime(arrivalTime: Long): String {
        if (arrivalTime <= 0) return context.getString(R.string.no_data)

        val minutes = arrivalTime / 60
        val seconds = arrivalTime % 60

        return when {
            minutes > 0 && seconds > 0 -> context.getString(R.string.minutes_seconds, minutes, seconds)
            minutes > 0 -> context.getString(R.string.minutes, minutes)
            else -> context.getString(R.string.seconds, seconds)
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
                color = Color(arrivalInfo?.getCongestionColor() ?: android.graphics.Color.GRAY),
                modifier = Modifier.weight(1.5f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
