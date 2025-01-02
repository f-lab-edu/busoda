package com.chaeny.busoda.stoplist

internal data class BusStop(
    val stopId: String,
    val stopName: String,
    val nextStopName: String
) {
    fun formatNextStopName(): String {
        return "$nextStopName 방면"
    }
}
