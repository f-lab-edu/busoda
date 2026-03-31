package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun addFavorite(stop: BusStop)
    suspend fun deleteFavorite(stopId: String)
    fun getFavoriteStops(): Flow<List<BusStop>>
    fun isFavorite(stopId: String): Flow<Boolean>
    suspend fun updateFavoriteOrders(stops: List<BusStop>)
}
