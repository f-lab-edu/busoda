package com.chaeny.busoda.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chaeny.busoda.database.dao.FavoriteBusDao
import com.chaeny.busoda.database.dao.FavoriteStopDao
import com.chaeny.busoda.database.model.FavoriteBus
import com.chaeny.busoda.database.model.FavoriteStop

@Database(entities = [FavoriteStop::class, FavoriteBus::class], version = 2, exportSchema = false)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteStopDao(): FavoriteStopDao
    abstract fun favoriteBusDao(): FavoriteBusDao
}
