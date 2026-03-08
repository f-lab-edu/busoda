package com.chaeny.busoda.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.CongestionLevel
import com.chaeny.busoda.ui.R

val LocalCurrentTime = compositionLocalOf<Long> { 0L }

@Composable
fun ArrivalTimeText(
    arrivalTime: Long?,
    modifier: Modifier = Modifier
) {
    val currentTime = LocalCurrentTime.current
    var displayTime by rememberSaveable { mutableStateOf("") }

    if (arrivalTime != null) {
        displayTime = setTextRemainingTime(arrivalTime, currentTime)
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
private fun setTextRemainingTime(arrivalTime: Long, currentTime: Long): String {
    val remainingTime = arrivalTime - currentTime
    return formattedArrivalTime(remainingTime)
}

@Composable
private fun formattedArrivalTime(arrivalTime: Long): String {
    val context = LocalContext.current

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
        CongestionLevel.HIGH -> colorResource(R.color.congestion_high)
        CongestionLevel.MEDIUM -> colorResource(R.color.congestion_medium)
        CongestionLevel.LOW -> colorResource(R.color.congestion_low)
        else -> colorResource(R.color.congestion_unknown)
    }
}
