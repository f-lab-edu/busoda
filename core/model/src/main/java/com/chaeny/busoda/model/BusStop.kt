package com.chaeny.busoda.model

data class BusStop(
    val stopId: String,
    val stopName: String,
    val nextStopName: String = ""
)
