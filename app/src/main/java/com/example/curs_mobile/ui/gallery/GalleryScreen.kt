package com.example.curs_mobile.ui.gallery

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private const val GALLERY_ROUTE = "gallery"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    viewModel: GalleryViewModel
) {
    val context = LocalContext.current
    val mediaList by viewModel.mediaList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedMedia by viewModel.selectedMedia.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionsState = rememberMultiplePermissionsState(requiredPermissions)
    val arePermissionsGranted = permissionsState.allPermissionsGranted

    LaunchedEffect(currentRoute, arePermissionsGranted) {
        if (currentRoute == GALLERY_ROUTE) {
            if (!arePermissionsGranted) {
                permissionsState.launchMultiplePermissionRequest()
            } else {
                viewModel.loadMedia(context)
            }
        }
    }

    BackHandler(enabled = isFullscreen) {
        viewModel.closeFullscreen()
    }

    GalleryScreenUI(
        hasPermissions = arePermissionsGranted,
        isLoading = isLoading,
        mediaList = mediaList,
        isFullscreen = isFullscreen,
        selectedMedia = selectedMedia,
        currentRoute = currentRoute,
        onMediaClick = { viewModel.openMedia(it) },
        onClose = { viewModel.closeFullscreen() },
        onDelete = { viewModel.removeSelected(context) },
        onNavigate = onNavigate
    )
}
