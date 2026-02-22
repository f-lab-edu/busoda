package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.FavoriteBusItem
import kotlinx.coroutines.flow.Flow

interface FavoriteBusRepository {
    suspend fun addFavoriteBus(stopId: String, stopName: String, busNumber: String, nextStopName: String)
    suspend fun deleteFavoriteBus(stopId: String, busNumber: String)
    fun getFavoriteBuses(): Flow<List<FavoriteBusItem>>
    fun isFavoriteBus(stopId: String, busNumber: String): Flow<Boolean>
}
