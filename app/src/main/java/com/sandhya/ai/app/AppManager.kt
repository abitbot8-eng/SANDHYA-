package com.sandhya.ai.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

data class AppEntry(
    val key: String,
    val label: String,
    val packages: List<String>
)

class AppManager(private val context: Context) {
    private val pm = context.packageManager

    val registry = listOf(
        AppEntry("youtube", "YouTube", listOf("com.google.android.youtube")),
        AppEntry("whatsapp", "WhatsApp", listOf("com.whatsapp")),
        AppEntry("instagram", "Instagram", listOf("com.instagram.android")),
        AppEntry("facebook", "Facebook", listOf("com.facebook.katana")),
        AppEntry("telegram", "Telegram", listOf("org.telegram.messenger")),
        AppEntry("maps", "Google Maps", listOf("com.google.android.apps.maps")),
        AppEntry("gmail", "Gmail", listOf("com.google.android.gm")),
        AppEntry("photos", "Google Photos", listOf("com.google.android.apps.photos")),
        AppEntry("drive", "Google Drive", listOf("com.google.android.apps.docs")),
        AppEntry("play_store", "Google Play Store", listOf("com.android.vending")),
        AppEntry("camera", "Camera", listOf("com.android.camera2", "com.google.android.GoogleCamera")),
        AppEntry("calculator", "Calculator", listOf("com.google.android.calculator", "com.android.calculator2")),
        AppEntry("clock", "Clock", listOf("com.google.android.deskclock")),
        AppEntry("contacts", "Contacts", listOf("com.google.android.contacts", "com.android.contacts")),
        AppEntry("phone", "Phone", listOf("com.google.android.dialer", "com.android.dialer")),
        AppEntry("settings", "Settings", listOf("android.settings"))
    )

    fun isInstalled(packageName: String): Boolean =
        try {
            pm.getApplicationInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) { false }

    fun findInstalled(key: String): String? =
        registry.firstOrNull { it.key == key }?.packages?.firstOrNull(::isInstalled)

    fun launch(key: String): Result<String> {
        if (key == "settings") {
            return runCatching {
                context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                "Settings"
            }
        }

        val pkg = findInstalled(key) ?: return Result.failure(
            IllegalStateException("${registry.firstOrNull { it.key == key }?.label ?: key} is not installed.")
        )

        val launchIntent = pm.getLaunchIntentForPackage(pkg)
            ?: return Result.failure(IllegalStateException("No launcher activity for $pkg"))

        return runCatching {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            registry.firstOrNull { it.key == key }?.label ?: pkg
        }
    }

    fun openPlayStoreFor(key: String): Boolean {
        val pkg = registry.firstOrNull { it.key == key }?.packages?.firstOrNull() ?: return false
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(market)
            true
        }.getOrDefault(false)
    }

    fun findByInstalledLabel(query: String): String? {
        val q = query.trim().lowercase()
        registry.firstOrNull {
            it.label.lowercase().contains(q) && findInstalled(it.key) != null
        }?.let { return it.key }

        // Dynamic discovery: enumerate apps that expose a launcher activity.
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val candidates = pm.queryIntentActivities(launcher, PackageManager.MATCH_ALL)
        return candidates.firstOrNull {
            it.loadLabel(pm).toString().lowercase().contains(q)
        }?.activityInfo?.packageName
    }
}
