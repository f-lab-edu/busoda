package com.chaeny.busoda.stopdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.CongestionLevel

@Composable
internal fun Dp.toSp(): TextUnit = with(LocalDensity.current) { toSp() }

@Composable
internal fun BusArrivalInfo.getCongestionText(): String {
    return when (congestion) {
        CongestionLevel.VERY_HIGH -> stringResource(R.string.congestion_very_high)
        CongestionLevel.HIGH -> stringResource(R.string.congestion_high)
        CongestionLevel.MEDIUM -> stringResource(R.string.congestion_medium)
        CongestionLevel.LOW -> stringResource(R.string.congestion_low)
        else -> stringResource(R.string.no_data)
    }
}

@Composable
internal fun BusArrivalInfo.getCongestionColor(): Color {
    return when (congestion) {
        CongestionLevel.VERY_HIGH -> colorResource(R.color.congestion_very_high)
        CongestionLevel.HIGH -> colorResource(R.color.congestion_high)
        CongestionLevel.MEDIUM -> colorResource(R.color.congestion_medium)
        CongestionLevel.LOW -> colorResource(R.color.congestion_low)
        else -> colorResource(R.color.congestion_unknown)
    }
}

@Composable
internal fun setTextRemainingTime(arrivalTime: Long, currentTime: Long): String {
    val remainingTime = arrivalTime - currentTime
    return formattedArrivalTime(remainingTime)
}

@Composable
internal fun formattedArrivalTime(arrivalTime: Long): String {
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
