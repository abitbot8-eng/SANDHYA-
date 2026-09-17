package com.sandhya.ai.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var view: TextView? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Boss, Overlay permission chahiye.", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        view = TextView(this).apply {
            text = "SANDHYA  •  🎙"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(28, 18, 28, 18)
            setBackgroundColor(0xDD16152A.toInt())
            setOnClickListener {
                Toast.makeText(this@OverlayService, "Listening mode is controlled by SANDHYA.", Toast.LENGTH_SHORT).show()
            }
        }

        val type = if (android.os.Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 18
            y = 120
        }

        windowManager?.addView(view, params)
    }

    override fun onDestroy() {
        view?.let { windowManager?.removeView(it) }
        view = null
        windowManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
