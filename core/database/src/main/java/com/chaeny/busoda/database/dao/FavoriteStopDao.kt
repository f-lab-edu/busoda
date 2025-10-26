package com.chaeny.busoda.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chaeny.busoda.database.model.FavoriteStop
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStopDao {
    @Query("SELECT * FROM favorite_stops")
    fun getFavorites(): Flow<List<FavoriteStop>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(stop: FavoriteStop)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stops WHERE stopId = :stopId LIMIT 1)")
    fun isFavorite(stopId: String): Flow<Boolean>
}
