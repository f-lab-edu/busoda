package com.chaeny.busoda.domain

import com.chaeny.busoda.data.repository.FavoriteBusRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
import javax.inject.Inject

class DefaultAddFavoriteBusUseCase @Inject constructor(
    private val favoriteBusRepository: FavoriteBusRepository,
    private val favoriteRepository: FavoriteRepository
) : AddFavoriteBusUseCase {
    override suspend operator fun invoke(
        stopId: String,
        stopName: String,
        busNumber: String,
        nextStopName: String
    ) {
        favoriteBusRepository.addFavoriteBus(stopId, stopName, busNumber, nextStopName)
        favoriteRepository.addFavorite(BusStop(stopId, stopName, nextStopName))
    }
}
