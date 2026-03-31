package com.chaeny.busoda.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stops")
data class FavoriteStop(
    @PrimaryKey val stopId: String,
    val stopName: String,
    val nextStopName: String,
    @ColumnInfo(defaultValue = "0") val order: Int = 0
)
