package com.chaeny.busoda.stopdetail

internal data class BusInfo(
    val busNumber: String,
    val nextStopName: String,
    val arrivalInfos: List<BusArrivalInfo>
)
