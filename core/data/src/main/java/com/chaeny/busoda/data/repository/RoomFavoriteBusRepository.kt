package com.chaeny.busoda.data.repository

import com.chaeny.busoda.database.dao.FavoriteBusDao
import com.chaeny.busoda.database.model.FavoriteBus
import com.chaeny.busoda.model.FavoriteBusItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomFavoriteBusRepository @Inject constructor(
    private val favoriteBusDao: FavoriteBusDao
) : FavoriteBusRepository {

    override suspend fun addFavoriteBus(stopId: String, stopName: String, busNumber: String, nextStopName: String) {
        favoriteBusDao.insertFavoriteBus(
            FavoriteBus(
                stopId = stopId,
                stopName = stopName,
                busNumber = busNumber,
                nextStopName = nextStopName
            )
        )
    }

    override suspend fun deleteFavoriteBus(stopId: String, busNumber: String) {
        favoriteBusDao.deleteFavoriteBus(stopId, busNumber)
    }

    override fun getFavoriteBuses(): Flow<List<FavoriteBusItem>> {
        return favoriteBusDao.getFavoriteBuses().map { entities ->
            entities.map { it.toFavoriteBusItem() }
        }
    }

    override fun isFavoriteBus(stopId: String, busNumber: String): Flow<Boolean> {
        return favoriteBusDao.isFavoriteBus(stopId, busNumber)
    }

    private fun FavoriteBus.toFavoriteBusItem() = FavoriteBusItem(
        stopId = stopId,
        stopName = stopName,
        busNumber = busNumber,
        nextStopName = nextStopName
    )
}
