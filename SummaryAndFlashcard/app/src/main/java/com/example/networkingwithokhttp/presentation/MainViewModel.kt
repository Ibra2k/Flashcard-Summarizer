package com.example.networkingwithokhttp.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkingwithokhttp.data.RetrofitInstance
import com.example.networkingwithokhttp.data.UploadFile
import com.example.networkingwithokhttp.domain.Constants
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream



class MainViewModel: ViewModel() {

    var response by mutableStateOf<UploadFile?>(null)
        private set

    var listOfResponses by mutableStateOf<List<UploadFile>>(emptyList())
        private set


    private fun uriToFile(context: Context, uri: Uri): File{
        //InputStream reads image bytes
        //OutputStream writes image bytes
        val file = File(context.cacheDir, "image/")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    private fun uriToPdfFile(context: Context, uri: Uri): File{
        //InputStream reads image bytes
        //OutputStream writes image bytes
        val file = File(context.cacheDir, "document/")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    // Function to send image to backend
    fun sendImage(context: Context, uri: Uri?) {
        val imageFile= uri?.let { uriToFile(context, it)  }
        val requestFile = imageFile?.asRequestBody("image/*".toMediaTypeOrNull())
        val body = requestFile?.let {
            MultipartBody.Part.createFormData("image", imageFile.name, it)
        }

        viewModelScope.launch {
            body?.let {body ->
                try {

                    response = RetrofitInstance.api.uploadImage(body)
                    listOfResponses = listOf(response!!)
                    Log.d(Constants.TAG, "Image $imageFile Uploaded Successfully")

                } catch (e: Exception) {
                    Log.d(Constants.TAG, "Upload failed: $e")
                }
            } ?: Log.d(Constants.TAG, "Body is null, cannot upload image")
        }
    }

    // Function to send image to backend
    fun sendFile(context: Context, uri: Uri?) {
        val pdfFile= uri?.let { uriToPdfFile(context, it)  }
        val requestFile = pdfFile?.asRequestBody("document/*".toMediaTypeOrNull())
        val body = requestFile?.let {
            MultipartBody.Part.createFormData("file", pdfFile.name, it)
        }

        viewModelScope.launch {
            body?.let {body ->
                try {

                    response = RetrofitInstance.api.uploadFile(body)
                    listOfResponses = listOf(response!!)
                    Log.d(Constants.TAG, "PDF $pdfFile Uploaded Successfully")

                } catch (e: Exception) {
                    Log.d(Constants.TAG, "Upload failed: $e")
                }
            } ?: Log.d(Constants.TAG, "Body is null, cannot upload image")
        }
    }



}
