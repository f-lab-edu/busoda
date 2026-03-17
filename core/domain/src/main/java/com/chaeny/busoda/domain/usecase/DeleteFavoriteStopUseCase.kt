package com.chaeny.busoda.domain.usecase

interface DeleteFavoriteStopUseCase {
    suspend operator fun invoke(stopId: String)
}
