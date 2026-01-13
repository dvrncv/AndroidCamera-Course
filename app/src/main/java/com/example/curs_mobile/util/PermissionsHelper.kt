package com.example.curs_mobile.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest

class PermissionsState(
    private val permissions: List<String>,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    private val context: Context
) {
    var allPermissionsGranted by mutableStateOf(checkAllPermissionsGranted())
        private set

    private fun checkAllPermissionsGranted(): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun launchPermissionRequest() {
        launcher.launch(permissions.toTypedArray())
    }

    internal fun updatePermissionsState() {
        allPermissionsGranted = checkAllPermissionsGranted()
    }
}

@Composable
fun rememberPermissionsState(permissions: List<String>): PermissionsState {
    val context = LocalContext.current
    
    val permissionsState = remember {
        mutableStateOf<PermissionsState?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        permissionsState.value?.updatePermissionsState()
    }

    if (permissionsState.value == null) {
        permissionsState.value = PermissionsState(permissions, launcher, context)
    }

    return permissionsState.value!!
}


fun getPhotoPermissions(): List<String> {
    return listOf(
        Manifest.permission.CAMERA
    )
}

fun getVideoPermissions(): List<String> {
    return listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
}

fun getMediaPermissions(): List<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

