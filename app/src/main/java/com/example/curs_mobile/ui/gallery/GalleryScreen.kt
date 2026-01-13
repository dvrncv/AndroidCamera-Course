package com.example.curs_mobile.ui.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.curs_mobile.util.getMediaPermissions
import com.example.curs_mobile.util.rememberPermissionsState

private const val GALLERY_ROUTE = "gallery"

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

    val permissionsState = rememberPermissionsState(getMediaPermissions())
    val arePermissionsGranted = permissionsState.allPermissionsGranted

    LaunchedEffect(currentRoute, arePermissionsGranted) {
        if (currentRoute == GALLERY_ROUTE) {
            if (!arePermissionsGranted) {
                permissionsState.launchPermissionRequest()
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
        onDelete = { viewModel.removeMedia(context) },
        onNavigate = onNavigate
    )
}
