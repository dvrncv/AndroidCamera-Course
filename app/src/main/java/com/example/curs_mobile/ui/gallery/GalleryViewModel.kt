package com.example.curs_mobile.ui.gallery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_mobile.model.MediaResource
import com.example.curs_mobile.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryViewModel : ViewModel() {
    
    private val mediaListState = MutableStateFlow<List<MediaResource>>(emptyList())
    val mediaList: StateFlow<List<MediaResource>> = mediaListState.asStateFlow()

    private val isLoadingState = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = isLoadingState.asStateFlow()

    private val selectedMediaState = MutableStateFlow<MediaResource?>(null)
    val selectedMedia: StateFlow<MediaResource?> = selectedMediaState.asStateFlow()

    private val isFullscreenState = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = isFullscreenState.asStateFlow()

    fun loadMedia(context: Context) {
        viewModelScope.launch {
            isLoadingState.value = true
            try {
                mediaListState.value = withContext(Dispatchers.IO) {
                    MediaRepository(context).loadMediaResources()
                }
            } finally {
                isLoadingState.value = false
            }
        }
    }

    fun removeMedia(context: Context, resource: MediaResource) {
        viewModelScope.launch {
            val isDeleted = withContext(Dispatchers.IO) {
                MediaRepository(context).deleteMedia(resource)
            }
            if (isDeleted) {
                updateMediaListAfterDeletion(resource)
            }
        }
    }

    private fun updateMediaListAfterDeletion(resource: MediaResource) {
        mediaListState.value = mediaListState.value.filter { it.uri != resource.uri }
        if (selectedMediaState.value?.uri == resource.uri) {
            closeFullscreen()
        }
    }

    fun openMedia(resource: MediaResource) {
        selectedMediaState.value = resource
        isFullscreenState.value = true
    }

    fun closeFullscreen() {
        isFullscreenState.value = false
        selectedMediaState.value = null
    }

    fun removeSelected(context: Context) {
        selectedMediaState.value?.let { removeMedia(context, it) }
    }
}
