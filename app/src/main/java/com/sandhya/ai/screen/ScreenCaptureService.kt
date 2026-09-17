package com.sandhya.ai.screen

import android.app.*
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.*
import androidx.core.app.NotificationCompat
import com.sandhya.ai.R

class ScreenCaptureService : Service() {
    private var projection: MediaProjection? = null

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("sandhya_capture", "SANDHYA Screen Capture", NotificationManager.IMPORTANCE_LOW)
        )
        val notification = NotificationCompat.Builder(this, "sandhya_capture")
            .setContentTitle("SANDHYA screen capture")
            .setContentText("Screen capture is active with your approval.")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                101,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(101, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val data = if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra("data", Intent::class.java)
        else @Suppress("DEPRECATION") intent.getParcelableExtra<Intent>("data")
        if (resultCode == Activity.RESULT_OK && data != null) {
            projection = getSystemService(android.media.projection.MediaProjectionManager::class.java)
                .getMediaProjection(resultCode, data)
            // A real capture pipeline should register a MediaProjection.Callback and create
            // a VirtualDisplay/ImageReader here for the exact feature being implemented.
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        projection?.stop()
        projection = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
