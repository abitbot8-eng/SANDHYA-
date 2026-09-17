package com.sandhya.ai.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {
    private val pm = context.packageManager

    fun microphoneGranted() = granted(Manifest.permission.RECORD_AUDIO)
    fun cameraGranted() = granted(Manifest.permission.CAMERA)
    fun notificationsGranted() =
        Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS)

    fun mediaReadable(): Boolean = when {
        Build.VERSION.SDK_INT >= 33 ->
            granted(Manifest.permission.READ_MEDIA_IMAGES) || granted(Manifest.permission.READ_MEDIA_VIDEO)
        else -> granted(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun overlayGranted() = Settings.canDrawOverlays(context)

    fun accessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(context.packageName)
    }

    fun batteryOptimizationIgnored(): Boolean {
        val power = context.getSystemService(PowerManager::class.java)
        return Build.VERSION.SDK_INT < 23 || power.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
