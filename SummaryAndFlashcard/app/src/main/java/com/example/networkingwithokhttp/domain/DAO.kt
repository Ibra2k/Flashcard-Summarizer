package com.example.networkingwithokhttp.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.networkingwithokhttp.data.ImageResponse

@Dao
interface ResponseDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: ImageResponse)

    @Query("SELECT * FROM imageResponse WHERE id = :id")
    suspend fun getResponseById(id: Int): ImageResponse?

    @Query("DELETE FROM imageResponse WHERE id = :id")
    suspend fun deleteResponseById(id: Int)

    @Query("SELECT * FROM imageResponse")
    suspend fun getAllResponses(): List<ImageResponse?>

    @Query("DELETE FROM imageResponse")
    suspend fun deleteAllResponses()

}