package com.chaeny.busoda.model

data class BusArrivalInfo(
    val arrivalTime: Long,
    val position: String,
    val congestion: CongestionLevel
)
