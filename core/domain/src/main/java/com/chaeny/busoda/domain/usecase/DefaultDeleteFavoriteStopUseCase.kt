package com.chaeny.busoda.domain.usecase

import com.chaeny.busoda.data.repository.FavoriteBusRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import javax.inject.Inject

class DefaultDeleteFavoriteStopUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val favoriteBusRepository: FavoriteBusRepository
) : DeleteFavoriteStopUseCase {
    override suspend operator fun invoke(stopId: String) {
        favoriteRepository.deleteFavorite(stopId)
        favoriteBusRepository.deleteFavoriteBusesByStop(stopId)
    }
}
