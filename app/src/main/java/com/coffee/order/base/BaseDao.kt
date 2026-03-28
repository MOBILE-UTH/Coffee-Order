package com.coffee.order.base

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Insert
    suspend fun insert(item: T): Long

    @Update
    suspend fun update(item: T)

    @Insert
    suspend fun insertAll(items: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(shopInfo: T)
}
