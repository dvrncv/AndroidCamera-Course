package com.example.curs_mobile.ui.photo

import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.takeOrElse
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

private const val ROUTE_GALLERY = "gallery"
private const val ROUTE_VIDEO = "video"
private const val ROUTE_PHOTO = "photo"

@Composable
fun PhotoScreenUI(
    preview: Preview?,
    focusRequest: Pair<UUID, Offset>,
    flash: Boolean,
    currentRoute: String,
    onPreviewViewCreated: (PreviewView) -> Unit,
    onTap: (Float, Float) -> Unit,
    onZoom: (Float) -> Unit,
    onTakePhoto: () -> Unit,
    onFlashEnd: () -> Unit,
    onSwitchCamera: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val focusCoordinates = remember(focusRequest.first) { focusRequest.second }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            preview = preview,
            onPreviewViewCreated = onPreviewViewCreated,
            onTap = onTap,
            onZoom = onZoom,
            modifier = Modifier.fillMaxSize()
        )

        FocusIndicator(
            focusCoordinates = focusCoordinates,
            isVisible = focusRequest.second.isSpecified,
            size = 48.dp
        )

        FlashOverlay(isVisible = flash, onFlashEnd = onFlashEnd)

        PhotoTakeButton(
            onTake = onTakePhoto,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
        )

        SwitchCameraButton(
            onClick = onSwitchCamera,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        PhotoBottomNavigation(
            currentRoute = currentRoute,
            onNavigateToGallery = { onNavigate(ROUTE_GALLERY) },
            onNavigateToVideo = { onNavigate(ROUTE_VIDEO) },
            onNavigateToPhoto = { onNavigate(ROUTE_PHOTO) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
fun PhotoTakeButton(
    onTake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationBorder = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Canvas(
        modifier = modifier
            .size(56.dp)
            .clickable(
                onClick = {
                    onTake()
                    coroutineScope.launch {
                        if (animationBorder.isRunning) animationBorder.snapTo(0f)
                        animationBorder.animateTo(1f)
                        delay(200)
                        animationBorder.animateTo(0f)
                    }
                },
                indication = null,
                interactionSource = null
            )
    ) {
        val borderSize = 2.dp.toPx()
        val minOffset = 4.dp.toPx()
        val maxOffset = ((size.minDimension / 2) - borderSize) / 3 * 2
        val outerRadius = size.minDimension / 2 - borderSize
        val innerRadius = outerRadius - (minOffset + (maxOffset - minOffset) * animationBorder.value)

        drawCircle(
            radius = outerRadius,
            color = Color.White,
            style = Stroke(width = borderSize)
        )
        drawCircle(
            radius = innerRadius,
            color = Color.White
        )
    }
}

@Composable
fun PhotoBottomNavigation(
    currentRoute: String,
    onNavigateToGallery: () -> Unit,
    onNavigateToVideo: () -> Unit,
    onNavigateToPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        Triple(ROUTE_GALLERY, "Галерея", onNavigateToGallery),
        Triple(ROUTE_VIDEO, "Видео", onNavigateToVideo),
        Triple(ROUTE_PHOTO, "Фото", onNavigateToPhoto)
    )

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        navItems.forEach { (route, label, onClick) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = onClick,
                icon = {},
                label = { Text(text = label, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
private fun CameraPreview(
    preview: Preview?,
    onPreviewViewCreated: (PreviewView) -> Unit,
    onTap: (Float, Float) -> Unit,
    onZoom: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PreviewView(context).also(onPreviewViewCreated)
        },
        update = { view ->
            preview?.setSurfaceProvider(view.surfaceProvider)
        },
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onTap(offset.x, offset.y)
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    onZoom(zoom)
                }
            }
    )
}

@Composable
private fun FocusIndicator(
    focusCoordinates: Offset,
    isVisible: Boolean,
    size: Dp
) {
    val position = focusCoordinates.takeOrElse { Offset.Zero }
    val halfSize = size.value / 2
    
    Box(
        modifier = Modifier
            .offset(
                x = (position.x.roundToInt() - halfSize.roundToInt()).dp,
                y = (position.y.roundToInt() - halfSize.roundToInt()).dp
            )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 2.dp.toPx()
                val circleRadius = (size.toPx() - strokeWidth) / 2
                drawCircle(
                    radius = circleRadius,
                    color = Color.White,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}

@Composable
private fun FlashOverlay(
    isVisible: Boolean,
    onFlashEnd: () -> Unit
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(50)
            onFlashEnd()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun SwitchCameraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(top = 32.dp, end = 16.dp)
            .background(Color.White.copy(alpha = 0.3f), CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.FlipCameraAndroid,
            contentDescription = "Переключить камеру",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
