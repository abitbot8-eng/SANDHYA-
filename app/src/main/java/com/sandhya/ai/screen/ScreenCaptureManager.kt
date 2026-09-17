package com.sandhya.ai.screen

import android.content.Context
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager

class ScreenCaptureManager(private val context: Context) {
    fun manager(): MediaProjectionManager =
        context.getSystemService(MediaProjectionManager::class.java)

    fun createProjection(resultCode: Int, data: android.content.Intent): MediaProjection =
        manager().getMediaProjection(resultCode, data)
}
