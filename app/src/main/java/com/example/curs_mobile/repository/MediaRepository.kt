package com.example.curs_mobile.repository

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.example.curs_mobile.model.MediaResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MediaRepository(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val isAndroidQOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    
    fun loadMediaResources(): List<MediaResource> {
        val resources = mutableListOf<MediaResource>()
        
        resources.addAll(loadImages())
        resources.addAll(loadVideos())
        
        return resources.sortedWith(
            compareByDescending<MediaResource> { it.dateAdded }
                .thenByDescending { it is MediaResource.Video }
        )
    }
    
    private fun loadImages(): List<MediaResource.Image> {
        val images = mutableListOf<MediaResource.Image>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
        )
        
        val selection = if (isAndroidQOrHigher) {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        } else {
            null
        }
        
        val selectionArgs = if (isAndroidQOrHigher) {
            arrayOf("%curs_mobile%")
        } else {
            null
        }
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: ""
                val dateAddedTimestamp = cursor.getLong(dateColumn)
                val dateAdded = Date(dateAddedTimestamp * 1000)
                val size = cursor.getLong(sizeColumn).toInt()
                
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                images.add(
                    MediaResource.Image(
                        uri = uri,
                        size = size,
                        name = name,
                        date = dateFormat.format(dateAdded),
                        dateAdded = dateAddedTimestamp
                    )
                )
            }
        }
        
        return images
    }
    
    
    private fun loadVideos(): List<MediaResource.Video> {
        val videos = mutableListOf<MediaResource.Video>()
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )
        
        val selection = if (isAndroidQOrHigher) {
            "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        } else {
            null
        }
        
        val selectionArgs = if (isAndroidQOrHigher) {
            arrayOf("%curs_mobile%")
        } else {
            null
        }
        
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: ""
                val dateAddedTimestamp = cursor.getLong(dateColumn)
                val dateAdded = Date(dateAddedTimestamp * 1000)
                val duration = cursor.getLong(durationColumn).toInt()
                val size = cursor.getLong(sizeColumn).toInt()
                
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                videos.add(
                    MediaResource.Video(
                        uri = uri,
                        size = size,
                        name = name,
                        date = dateFormat.format(dateAdded),
                        dateAdded = dateAddedTimestamp,
                        duration = duration
                    )
                )
            }
        }
        
        return videos
    }
    
    
    fun deleteMedia(resource: MediaResource): Boolean {
        return try {
            val deleted = context.contentResolver.delete(resource.uri, null, null)
            deleted > 0
        } catch (e: Exception) {
            false
        }
    }
}
