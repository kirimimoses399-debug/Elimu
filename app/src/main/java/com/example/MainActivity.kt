package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.ElimuHubAppContent
import com.example.ui.ElimuHubViewModel
import com.example.ui.ElimuHubViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Support full-bleed edge-to-edge layouts (Safe drawing and navigation bars)
        enableEdgeToEdge()

        // Initialize local Room database
        val database = AppDatabase.getDatabase(this)
        val appDao = database.appDao()
        val repository = AppRepository(appDao)

        // Initialize Firebase SDK Client (Realtime Database API over HTTP)
        com.example.network.FirebaseApiClient.initialize(applicationContext)

        // Instantiate core ViewModel with factory
        val viewModelFactory = ElimuHubViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ElimuHubViewModel::class.java]

        setContent {
            MyApplicationTheme {
                ElimuHubAppContent(viewModel = viewModel)
            }
        }
    }
}
