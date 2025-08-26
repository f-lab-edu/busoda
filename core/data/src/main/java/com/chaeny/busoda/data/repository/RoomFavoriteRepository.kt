package com.chaeny.busoda.data.repository

import com.chaeny.busoda.database.dao.FavoriteStopDao
import com.chaeny.busoda.database.model.FavoriteStop
import com.chaeny.busoda.model.BusStop
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoriteRepository @Inject constructor(
    private val favoriteStopDao: FavoriteStopDao
) : FavoriteRepository {

    override suspend fun addFavorite(stop: BusStop) {
        favoriteStopDao.insertFavorite(
            FavoriteStop(
                stop.stopId,
                stop.stopName,
                stop.nextStopName
            )
        )
    }

    override fun getFavorites(): Flow<List<BusStop>> {
        return favoriteStopDao.getFavorites().map { entities ->
            entities.map { entity ->
                BusStop(
                    entity.stopId,
                    entity.stopName,
                    entity.nextStopName
                )
            }
        }
    }
}
