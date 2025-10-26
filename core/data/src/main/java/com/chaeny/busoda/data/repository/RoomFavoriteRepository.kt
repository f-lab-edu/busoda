package com.chaeny.busoda.data.repository

import com.chaeny.busoda.database.dao.FavoriteStopDao
import com.chaeny.busoda.database.model.FavoriteStop
import com.chaeny.busoda.model.BusStop
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoriteRepository @Inject constructor(
    private val favoriteStopDao: FavoriteStopDao
) : FavoriteRepository {

    override suspend fun addFavorite(stop: BusStop) {
        favoriteStopDao.insertFavorite(stop.toFavoriteStop())
    }

    override fun getFavorites(): Flow<List<BusStop>> {
        return favoriteStopDao.getFavorites().map { entities ->
            entities.map { entity -> entity.toBusStop() }
        }
    }

    override fun isFavorite(stopId: String) = favoriteStopDao.isFavorite(stopId)

    private fun BusStop.toFavoriteStop() = FavoriteStop(stopId, stopName, nextStopName)

    private fun FavoriteStop.toBusStop() = BusStop(stopId, stopName, nextStopName)
}
