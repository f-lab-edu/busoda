package com.chaeny.busoda.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.chaeny.busoda.database.dao.FavoriteBusDao
import com.chaeny.busoda.database.dao.FavoriteStopDao
import com.chaeny.busoda.database.model.FavoriteBus
import com.chaeny.busoda.database.model.FavoriteStop

@Database(
    entities = [FavoriteStop::class, FavoriteBus::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 2, to = 3)]
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteStopDao(): FavoriteStopDao
    abstract fun favoriteBusDao(): FavoriteBusDao
}
