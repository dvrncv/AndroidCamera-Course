package com.example.curs_mobile.ui.photo

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation

class PhotoViewModel : ViewModel() {
    
    private var meteringFactory: SurfaceOrientedMeteringPointFactory? = null
    private var cameraControl: CameraControl? = null
    private var previewInstance: Preview? = null
    private var imageCapture: ImageCapture? = null

    fun initializePreviewView(view: PreviewView) {
        meteringFactory = SurfaceOrientedMeteringPointFactory(
            view.width.toFloat(),
            view.height.toFloat()
        )
    }

    suspend fun bindToCamera(
        appContext: Context,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
        onPreviewReady: (Preview) -> Unit
    ) {
        val provider = ProcessCameraProvider.awaitInstance(appContext)
        provider.unbindAll()
        
        previewInstance = Preview.Builder().build()
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        val camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            previewInstance,
            imageCapture
        )
        
        cameraControl = camera.cameraControl
        previewInstance?.let(onPreviewReady)
        
        try {
            awaitCancellation()
        } finally {
            cameraControl = null
        }
    }

    fun takePhoto(context: Context) {
        val capture = imageCapture ?: return
        
        val metadata = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/curs_mobile")
        }
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            metadata
        ).build()
        
        val callback = object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {}
            override fun onError(exc: androidx.camera.core.ImageCaptureException) {}
        }
        
        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            callback
        )
    }

    fun setFocus(x: Float, y: Float) {
        val factory = meteringFactory ?: return
        val control = cameraControl ?: return
        
        val point = factory.createPoint(x, y) ?: return
        val action = FocusMeteringAction.Builder(point).build()
        control.startFocusAndMetering(action)
    }

    fun setZoom(zoomLevel: Float) {
        cameraControl?.setLinearZoom(zoomLevel.coerceIn(0f, 1f))
    }
}
