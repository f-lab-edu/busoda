package com.chaeny.busoda.stopdetail

internal data class Bus(
    val busNumber: String,
    val stopName: String,
    val nextStopName: String,
    val arrivalInfos: List<BusArrivalInfo>
)
