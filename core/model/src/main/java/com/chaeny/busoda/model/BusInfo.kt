package com.chaeny.busoda.model

data class BusInfo(
    val busNumber: String,
    val nextStopName: String,
    val arrivalInfos: List<BusArrivalInfo>
)
