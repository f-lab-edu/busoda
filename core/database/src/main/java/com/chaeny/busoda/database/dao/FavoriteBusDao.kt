package com.chaeny.busoda.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chaeny.busoda.database.model.FavoriteBus
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBusDao {
    @Query("SELECT * FROM favorite_buses")
    fun getFavoriteBuses(): Flow<List<FavoriteBus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteBus(bus: FavoriteBus)

    @Query("DELETE FROM favorite_buses WHERE stopId = :stopId AND busNumber = :busNumber")
    suspend fun deleteFavoriteBus(stopId: String, busNumber: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_buses WHERE stopId = :stopId AND busNumber = :busNumber LIMIT 1)")
    fun isFavoriteBus(stopId: String, busNumber: String): Flow<Boolean>
}
