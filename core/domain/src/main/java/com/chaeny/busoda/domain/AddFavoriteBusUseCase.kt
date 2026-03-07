package com.chaeny.busoda.domain

interface AddFavoriteBusUseCase {
    suspend operator fun invoke(
        stopId: String,
        stopName: String,
        busNumber: String,
        nextStopName: String
    )
}
