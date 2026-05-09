package com.example.networkingwithokhttp.data

import okhttp3.MultipartBody
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class UploadFile(
    val filename: String,
    val summary: String = "",
    val questions: String = ""
)

interface ApiService {

    @Multipart
    @POST("/upload_image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): UploadFile


    @Multipart
    @POST("/upload_file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadFile
}