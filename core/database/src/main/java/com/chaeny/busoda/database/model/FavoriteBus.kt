package com.chaeny.busoda.database.model

import androidx.room.Entity

@Entity(
    tableName = "favorite_buses",
    primaryKeys = ["stopId", "busNumber"]
)
data class FavoriteBus(
    val stopId: String,
    val stopName: String,
    val busNumber: String,
    val nextStopName: String
)
