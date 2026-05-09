package com.example.networkingwithokhttp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "imageResponse")
data class ImageResponse(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id")
    val id: Int,
    @SerializedName("summary")
    val summary: String,
    @SerializedName("questions")
    val questions: String
)
