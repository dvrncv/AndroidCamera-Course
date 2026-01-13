package com.example.curs_mobile.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.curs_mobile.model.MediaResource

private const val ROUTE_GALLERY = "gallery"
private const val ROUTE_VIDEO = "video"
private const val ROUTE_PHOTO = "photo"

@Composable
fun GalleryScreenUI(
    hasPermissions: Boolean,
    isLoading: Boolean,
    mediaList: List<MediaResource>,
    isFullscreen: Boolean,
    selectedMedia: MediaResource?,
    currentRoute: String,
    onMediaClick: (MediaResource) -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !hasPermissions -> PermissionDeniedView()
            isLoading -> LoadingIndicator()
            mediaList.isEmpty() -> EmptyGalleryView()
            else -> MediaGrid(
                mediaList = mediaList,
                onMediaClick = onMediaClick
            )
        }

        if (isFullscreen && selectedMedia != null) {
            FullscreenMediaView(
                resource = selectedMedia,
                onClose = onClose,
                onDelete = onDelete
            )
        }

        if (!isFullscreen) {
            GalleryBottomNavigation(
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
}

@Composable
fun PermissionDeniedView(modifier: Modifier = Modifier) {
    CenteredContainer(modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = "Нет доступа к медиафайлам",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Разрешите доступ к фото и видео в настройках",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    CenteredContainer(modifier) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyGalleryView(modifier: Modifier = Modifier) {
    CenteredContainer(modifier) {
        Text(
            text = "Медиа не найдено",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun MediaGrid(
    mediaList: List<MediaResource>,
    onMediaClick: (MediaResource) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(top = 80.dp, start = 12.dp, end = 12.dp, bottom = 150.dp),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(mediaList) { resource ->
            MediaThumbnail(
                resource = resource,
                onClick = { onMediaClick(resource) }
            )
        }
    }
}

@Composable
private fun MediaThumbnail(resource: MediaResource, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().clickable(onClick = onClick)) {
            MediaDisplay(
                resource = resource,
                modifier = Modifier.fillMaxSize(),
                isFullscreen = false
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = getMediaTypeIcon(resource),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = resource.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FullscreenMediaView(
    resource: MediaResource,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonPadding = Modifier.padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .zIndex(1f)
    ) {
        MediaDisplay(
            resource = resource,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 32.dp,
                    bottom = if (resource is MediaResource.Video) 50.dp else 0.dp
                ),
            isFullscreen = true
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .then(buttonPadding)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .then(buttonPadding)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun GalleryBottomNavigation(
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
private fun CenteredContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

private fun getMediaTypeIcon(resource: MediaResource) = when (resource) {
    is MediaResource.Image -> Icons.Filled.PhotoCamera
    is MediaResource.Video -> Icons.Filled.VideoLibrary
}

@Composable
private fun MediaDisplay(
    resource: MediaResource,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false
) {
    when (resource) {
        is MediaResource.Image -> {
            AsyncImage(
                model = resource.uri,
                contentDescription = null,
                modifier = modifier,
                contentScale = if (isFullscreen) ContentScale.Fit else ContentScale.Crop
            )
        }
        is MediaResource.Video -> {
            if (isFullscreen) {
                VideoPlayer(
                    uri = resource.uri,
                    showController = true,
                    modifier = modifier
                )
            } else {
                VideoPreviewImage(
                    uri = resource.uri,
                    modifier = modifier,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun VideoPreviewImage(
    uri: Uri,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var thumbnail by remember(uri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        thumbnail = try {
            val videoId = ContentUris.parseId(uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(
                    uri,
                    Size(512, 512),
                    null
                )
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver,
                    videoId,
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    thumbnail?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
private fun VideoPlayer(uri: Uri, modifier: Modifier = Modifier, showController: Boolean = true) {
    val context = LocalContext.current
    
    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }
    
    DisposableEffect(uri) {
        onDispose { exoPlayer.release() }
    }
    
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = showController
            }
        },
        modifier = modifier
    )
}

