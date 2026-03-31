package com.chaeny.busoda.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.chaeny.busoda.database.model.FavoriteStop
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStopDao {
    @Query("SELECT * FROM favorite_stops ORDER BY `order` ASC")
    fun getFavorites(): Flow<List<FavoriteStop>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(stop: FavoriteStop)

    @Query("DELETE FROM favorite_stops WHERE stopId = :stopId")
    suspend fun deleteFavorite(stopId: String)

    @Query("UPDATE favorite_stops SET `order` = :order WHERE stopId = :stopId")
    suspend fun updateOrder(stopId: String, order: Int)

    @Transaction
    suspend fun updateAllOrders(stops: List<Pair<String, Int>>) {
        stops.forEach { (stopId, order) -> updateOrder(stopId, order) }
    }

    @Query("SELECT COALESCE(MAX(`order`), -1) + 1 FROM favorite_stops")
    suspend fun getNextOrder(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stops WHERE stopId = :stopId LIMIT 1)")
    fun isFavorite(stopId: String): Flow<Boolean>
}
