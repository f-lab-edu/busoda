package com.chaeny.busoda.domain.usecase

interface AddFavoriteBusUseCase {
    suspend operator fun invoke(
        stopId: String,
        stopName: String,
        busNumber: String,
        nextStopName: String
    )
}
