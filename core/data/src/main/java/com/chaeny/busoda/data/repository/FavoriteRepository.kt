package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop

interface FavoriteRepository {
    suspend fun getFavorites(): List<BusStop>
}
