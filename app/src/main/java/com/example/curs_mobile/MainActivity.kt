package com.example.curs_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.curs_mobile.ui.gallery.GalleryScreen
import com.example.curs_mobile.ui.gallery.GalleryViewModel
import com.example.curs_mobile.ui.photo.PhotoScreen
import com.example.curs_mobile.ui.photo.PhotoViewModel
import com.example.curs_mobile.ui.theme.Curs_mobileTheme
import com.example.curs_mobile.ui.video.VideoScreen
import com.example.curs_mobile.ui.video.VideoViewModel

private const val ROUTE_PHOTO = "photo"
private const val ROUTE_VIDEO = "video"
private const val ROUTE_GALLERY = "gallery"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Curs_mobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by rememberSaveable { mutableStateOf(ROUTE_PHOTO) }
                    val backStack = remember { mutableListOf(ROUTE_PHOTO) }
                    
                    val photoViewModel: PhotoViewModel = viewModel()
                    val videoViewModel: VideoViewModel = viewModel()
                    val galleryViewModel: GalleryViewModel = viewModel()

                    val onNavigate: (String) -> Unit = { route ->
                        if (route != currentScreen) {
                            backStack.removeAll { it == route }
                            backStack.add(route)
                            currentScreen = route
                        }
                    }

                    BackHandler(enabled = currentScreen != ROUTE_PHOTO) {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                            currentScreen = backStack.last()
                        }
                    }

                    when (currentScreen) {
                        ROUTE_PHOTO -> PhotoScreen(
                            currentRoute = currentScreen,
                            onNavigate = onNavigate,
                            viewModel = photoViewModel
                        )
                        ROUTE_VIDEO -> VideoScreen(
                            currentRoute = currentScreen,
                            onNavigate = onNavigate,
                            viewModel = videoViewModel
                        )
                        ROUTE_GALLERY -> GalleryScreen(
                            currentRoute = currentScreen,
                            onNavigate = onNavigate,
                            viewModel = galleryViewModel
                        )
                    }
                }
            }
        }
    }
}
