package com.example.curs_mobile.ui.video

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.ExperimentalPersistentRecording
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel() {
    
    private var meteringFactory: SurfaceOrientedMeteringPointFactory? = null
    private var cameraControl: CameraControl? = null
    private var previewInstance: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwnerInstance: LifecycleOwner? = null
    private var currentRecording: Recording? = null

    private val recordingState = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = recordingState.asStateFlow()

    private val durationState = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = durationState.asStateFlow()

    fun initializePreviewView(view: androidx.camera.view.PreviewView) {
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
        cameraProvider = cameraProvider ?: ProcessCameraProvider.awaitInstance(appContext)
        lifecycleOwnerInstance = lifecycleOwner
        
        cameraProvider?.unbindAll()

        previewInstance = Preview.Builder().build()
        videoCapture = VideoCapture.withOutput(Recorder.Builder().build())

        val camera = cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            previewInstance,
            videoCapture
        )
        
        cameraControl = camera?.cameraControl
        previewInstance?.let(onPreviewReady)
        
        try {
            awaitCancellation()
        } finally {
            cameraControl = null
        }
    }

    fun changeCamera(cameraSelector: CameraSelector, onPreviewReady: (Preview) -> Unit) {
        viewModelScope.launch {
            val provider = cameraProvider ?: return@launch
            val owner = lifecycleOwnerInstance ?: return@launch
            val capture = videoCapture ?: return@launch

            provider.unbindAll()

            val preview = Preview.Builder().build()

            val camera = provider.bindToLifecycle(
                owner,
                cameraSelector,
                preview,
                capture
            )
            
            cameraControl = camera.cameraControl
            previewInstance = preview
            onPreviewReady(preview)
        }
    }

    @OptIn(ExperimentalPersistentRecording::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun toggleRecording(context: Context) {
        if (currentRecording != null) {
            stopRecording()
        } else {
            startRecording(context)
        }
    }

    private fun stopRecording() {
        currentRecording?.apply {
            stop()
            currentRecording = null
            recordingState.value = false
        }
    }

    @OptIn(ExperimentalPersistentRecording::class)
    private fun startRecording(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        val metadata = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Video_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/curs_mobile")
        }
        
        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(metadata).build()
        
        try {
            videoCapture?.output
                ?.prepareRecording(context, outputOptions)
                ?.asPersistentRecording()
                ?.withAudioEnabled()
                ?.start(ContextCompat.getMainExecutor(context), ::processRecordingEvent)
                ?.also { currentRecording = it }
        } catch (e: SecurityException) {
        }
    }

    private fun processRecordingEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> recordingState.value = true
            is VideoRecordEvent.Status -> {
                durationState.value = event.recordingStats.recordedDurationNanos / 1_000_000
            }
            is VideoRecordEvent.Finalize -> {
                recordingState.value = false
                if (event.hasError()) {
                    currentRecording?.close()
                    currentRecording = null
                }
            }
        }
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
