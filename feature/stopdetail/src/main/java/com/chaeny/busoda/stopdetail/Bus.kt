package com.chaeny.busoda.stopdetail

internal data class Bus(
    val busNumber: String,
    val nextStopName: String,
    val firstArrivalTime: String,
    val firstPosition: String,
    val firstCongestion: String,
    val secondArrivalTime: String,
    val secondPosition: String,
    val secondCongestion: String
)
