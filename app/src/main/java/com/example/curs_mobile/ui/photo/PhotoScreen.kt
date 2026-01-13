package com.example.curs_mobile.ui.photo

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.curs_mobile.util.getPhotoPermissions
import com.example.curs_mobile.util.rememberPermissionsState
import kotlinx.coroutines.delay
import java.util.UUID

private const val PHOTO_ROUTE = "photo"

@Composable
fun PhotoScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    viewModel: PhotoViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionsState = rememberPermissionsState(getPhotoPermissions())
    val arePermissionsGranted = permissionsState.allPermissionsGranted

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var currentZoom by remember { mutableStateOf(0f) }
    var isFlashVisible by remember { mutableStateOf(false) }
    var focusRequest by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }
    var preview by remember { mutableStateOf<Preview?>(null) }

    LaunchedEffect(focusRequest) {
        if (focusRequest.second.isSpecified) {
            delay(1000)
            focusRequest = focusRequest.first to Offset.Unspecified
        }
    }

    LaunchedEffect(currentRoute, arePermissionsGranted) {
        if (currentRoute == PHOTO_ROUTE) {
            if (!arePermissionsGranted) {
                permissionsState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(currentRoute, lifecycleOwner, cameraSelector, arePermissionsGranted) {
        if (currentRoute == PHOTO_ROUTE && arePermissionsGranted) {
            viewModel.bindToCamera(
                appContext = context.applicationContext,
                lifecycleOwner = lifecycleOwner,
                cameraSelector = cameraSelector,
                onPreviewReady = { preview = it }
            )
        }
    }

    PhotoScreenUI(
        preview = preview,
        focusRequest = focusRequest,
        flash = isFlashVisible,
        currentRoute = currentRoute,
        onPreviewViewCreated = { viewModel.initializePreviewView(it) },
        onTap = { x, y ->
            viewModel.setFocus(x, y)
            focusRequest = UUID.randomUUID() to Offset(x, y)
        },
        onZoom = { ratio ->
            currentZoom = (currentZoom - (1f - ratio)).coerceIn(0f, 1f)
            viewModel.setZoom(currentZoom)
        },
        onTakePhoto = {
            viewModel.takePhoto(context)
            isFlashVisible = true
        },
        onFlashEnd = { isFlashVisible = false },
        onSwitchCamera = {
            cameraSelector = when (cameraSelector) {
                CameraSelector.DEFAULT_BACK_CAMERA -> CameraSelector.DEFAULT_FRONT_CAMERA
                else -> CameraSelector.DEFAULT_BACK_CAMERA
            }
        },
        onNavigate = onNavigate
    )
}
