package com.example.networkingwithokhttp.domain

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.networkingwithokhttp.data.ImageResponse

@Database(version = 1, entities = [ImageResponse::class], exportSchema = false)
abstract class ImageResponseDatabase: RoomDatabase() {

    abstract fun getImageResponseDao(): ResponseDAO
}