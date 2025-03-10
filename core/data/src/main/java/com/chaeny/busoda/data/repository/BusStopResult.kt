package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop

class BusStopResult(
    val data: List<BusStop> = emptyList(),
    val isNetworkError: Boolean = false,
    val isNoResult: Boolean = false
)
