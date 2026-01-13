package com.example.curs_mobile.model

import android.net.Uri

sealed class MediaResource {
    abstract val uri: Uri
    abstract val size: Int
    abstract val name: String
    abstract val date: String
    abstract val dateAdded: Long

    data class Image(
        override val uri: Uri,
        override val size: Int,
        override val name: String,
        override val date: String,
        override val dateAdded: Long
    ) : MediaResource()

    data class Video(
        override val uri: Uri,
        override val size: Int,
        override val name: String,
        override val date: String,
        override val dateAdded: Long,
        val duration: Int
    ) : MediaResource()
}
