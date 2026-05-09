package com.example.networkingwithokhttp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.networkingwithokhttp.ui.theme.NetworkingWithOkHTTPTheme
import com.example.networkingwithokhttp.presentation.MainScreen
import com.example.networkingwithokhttp.presentation.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetworkingWithOkHTTPTheme {

                val viewModel = MainViewModel()
                MainScreen(viewModel)

            }
        }
    }
}
