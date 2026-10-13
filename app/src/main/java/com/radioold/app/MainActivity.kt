package com.radioold.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.radioold.app.data.catalog.CatalogRepository
import com.radioold.app.playback.PlayerController
import com.radioold.app.ui.screens.MainScreen
import com.radioold.app.ui.theme.RadiooldTheme
import com.radioold.app.viewmodel.RadiooldViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext = applicationContext

        // Manual DI: the ViewModel owns the repository and player, so playback
        // survives configuration changes and is released in onCleared().
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RadiooldViewModel(
                    CatalogRepository(appContext),
                    PlayerController(appContext),
                ) as T
        }

        setContent {
            RadiooldTheme {
                val viewModel: RadiooldViewModel = viewModel(factory = factory)
                MainScreen(viewModel)
            }
        }
    }
}
