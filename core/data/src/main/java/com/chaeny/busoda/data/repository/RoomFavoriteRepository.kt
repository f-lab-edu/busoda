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
        val order = favoriteStopDao.getNextOrder()
        favoriteStopDao.insertFavorite(stop.toFavoriteStop(order))
    }

    override suspend fun deleteFavorite(stopId: String) {
        favoriteStopDao.deleteFavorite(stopId)
    }

    override fun getFavoriteStops(): Flow<List<BusStop>> {
        return favoriteStopDao.getFavorites().map { entities ->
            entities.map { entity -> entity.toBusStop() }
        }
    }

    override fun isFavorite(stopId: String) = favoriteStopDao.isFavorite(stopId)

    override suspend fun updateFavoriteOrders(stops: List<BusStop>) {
        favoriteStopDao.updateAllOrders(
            stops.mapIndexed { index, stop -> stop.stopId to index }
        )
    }

    private fun BusStop.toFavoriteStop(order: Int) = FavoriteStop(stopId, stopName, nextStopName, order)

    private fun FavoriteStop.toBusStop() = BusStop(stopId, stopName, nextStopName)
}
