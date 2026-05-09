package com.example.networkingwithokhttp.presentation

import  android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.networkingwithokhttp.data.UploadFile
import com.example.networkingwithokhttp.domain.Constants
import com.example.networkingwithokhttp.R

@Composable
fun MainScreen(viewModel: MainViewModel) {


    val context = LocalContext.current
    val listOfResponses = viewModel.listOfResponses


    // Variable to hold my image picked URI
    var selectedImageUri: Uri? by remember { mutableStateOf(null) }

    var selectedFileUri: Uri? by remember { mutableStateOf(null) }

    // Variable to launch an image picker
    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ){  uri ->
            if (uri != null) {
                selectedImageUri = uri
                Log.d(Constants.TAG, "Selected Image Uri: $uri")
            } else
                Log.d(Constants.TAG, "No image Selected")
        }



    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ){ fileUri ->
            if(fileUri != null){
                selectedFileUri = fileUri
                Log.d(Constants.TAG, "Selected File Uri: $fileUri")
            } else {
                Log.d(Constants.TAG, "No File Selected")
            }
        }


        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Image Uploading Section
            item {

                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    model = selectedImageUri ,
                    contentDescription = "Selected Image",
                    fallback = painterResource(id = R.drawable.stock_image),
                )
            }


            // Two Buttons to Upload the Image on the screen
            // And Send Image to send to backend for ai to summarize
            item {
                Button(onClick = {
                    if(selectedImageUri != null){
                        viewModel.sendImage(context, selectedImageUri)
                    } else if (selectedFileUri != null) {
                        viewModel.sendFile(context, selectedFileUri)
                    }
                }){
                    Text("Send")
                }

                Button(onClick = {
                    imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Upload Image") }

                Button(onClick = {
                    filePickerLauncher.launch(arrayOf("application/pdf"))
                }) { Text("Upload File") }




                Spacer(modifier = Modifier.height(16.dp))
            }



            // Text field of summarized image and the questions (for flash cards)
            items(listOfResponses) { imageResponse ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Summary:", color = Color.Black)
                    Text(imageResponse.summary, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Questions:", color = Color.Black)
                    Text(imageResponse.questions, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

        }


    }


















