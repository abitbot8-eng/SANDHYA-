package com.sandhya.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sandhya.ai.app.AppManager
import com.sandhya.ai.command.CommandParser
import com.sandhya.ai.command.CommandType
import com.sandhya.ai.core.TaskManager
import com.sandhya.ai.permissions.PermissionManager
import com.sandhya.ai.voice.VoiceForegroundService

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager
    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            VoiceForegroundService.startScreenCapture(this, result.resultCode, result.data!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)

        setContent {
            SandhyaTheme {
                SandhyaScreen(
                    permissionManager = permissionManager,
                    onMic = { startVoice() },
                    onPermission = { key -> openPermission(key) },
                    onScreenCapture = { requestScreenCapture() }
                )
            }
        }
    }

    private fun startVoice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        VoiceForegroundService.start(this)
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(MediaProjectionManager::class.java)
        mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
    }

    private fun openPermission(key: String) {
        when (key) {
            "mic" -> micPermission.launch(Manifest.permission.RECORD_AUDIO)
            "camera" -> cameraPermission.launch(Manifest.permission.CAMERA)
            "notifications" -> if (Build.VERSION.SDK_INT >= 33)
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            "accessibility" -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            "overlay" -> {
                if (Settings.canDrawOverlays(this)) {
                    startService(Intent(this, com.sandhya.ai.overlay.OverlayService::class.java))
                } else {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            }
            "battery" -> {
                val pm = getSystemService(PowerManager::class.java)
                if (Build.VERSION.SDK_INT >= 23 && !pm.isIgnoringBatteryOptimizations(packageName)) {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")))
                }
            }
            "media" -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            })
        }
    }
}

@Composable
private fun SandhyaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF05070D),
            surface = Color(0xFF0C1020),
            primary = Color(0xFF9C7BFF),
            secondary = Color(0xFF56E0FF)
        ),
        content = content
    )
}

@Composable
private fun SandhyaScreen(
    permissionManager: PermissionManager,
    onMic: () -> Unit,
    onPermission: (String) -> Unit,
    onScreenCapture: () -> Unit
) {
    var task by remember { mutableStateOf("Waiting for you, Boss…") }
    val infinite = rememberInfiniteTransition(label = "orb")
    val scale by infinite.animateFloat(
        1f, 1.08f,
        infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF090B18), Color(0xFF03040A)))
        )
    ) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Text("SANDHYA", style = MaterialTheme.typography.headlineLarge)
            Text("Your futuristic phone assistant", color = Color(0xFF8F98AE))

            Spacer(Modifier.height(36.dp))
            Box(
                Modifier.size(190.dp).scale(scale).background(
                    Brush.radialGradient(
                        listOf(Color(0xFFB58CFF), Color(0xFF2C1D65), Color.Transparent)
                    ),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(76.dp), tint = Color.White)
            }

            Spacer(Modifier.height(24.dp))
            Text(task, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(24.dp))
            FilledIconButton(
                onClick = {
                    task = "Listening…"
                    onMic()
                },
                modifier = Modifier.size(78.dp)
            ) {
                Icon(Icons.Default.Mic, "Speak")
            }

            Spacer(Modifier.height(28.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Android Permissions", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    PermissionRow("🎤 Microphone", permissionManager.microphoneGranted(), "mic", onPermission)
                    PermissionRow("♿ Accessibility", permissionManager.accessibilityEnabled(), "accessibility", onPermission)
                    PermissionRow("🪟 Overlay", permissionManager.overlayGranted(), "overlay", onPermission)
                    PermissionRow("📺 Screen Capture", false, "screen", { onScreenCapture() })
                    PermissionRow("🔔 Notifications", permissionManager.notificationsGranted(), "notifications", onPermission)
                    PermissionRow("🔋 Battery", permissionManager.batteryOptimizationIgnored(), "battery", onPermission)
                    PermissionRow("📷 Camera", permissionManager.cameraGranted(), "camera", onPermission)
                    PermissionRow("📂 Photos/Media", permissionManager.mediaReadable(), "media", onPermission)
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    granted: Boolean,
    key: String,
    onClick: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f))
        Text(if (granted) "✓ Allowed" else "⚠ Needs permission")
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = { onClick(key) }) { Text("Open") }
    }
}
