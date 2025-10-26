package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun addFavorite(stop: BusStop)
    fun getFavorites(): Flow<List<BusStop>>
    fun isFavorite(stopId: String): Flow<Boolean>
}
