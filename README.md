# SANDHYA — native Android AI phone assistant

SANDHYA is a native Kotlin/Jetpack Compose Android app. It does not use browser URLs to launch installed apps. `AppManager` resolves installed packages with `PackageManager` and starts their launcher activities with Android intents.

## Supported native launches
- YouTube: `com.google.android.youtube`
- WhatsApp: `com.whatsapp`
- Instagram: `com.instagram.android`
- Facebook: `com.facebook.katana`
- Telegram: `org.telegram.messenger`
- Google Maps: `com.google.android.apps.maps`
- Gmail: `com.google.android.gm`
- Google Photos: `com.google.android.apps.photos`
- Google Drive: `com.google.android.apps.docs`
- Play Store: `com.android.vending`
- Camera: common camera packages
- Calculator / Clock / Contacts / Phone
- Settings: Android `ACTION_SETTINGS`

The registry is only a fast path; `PackageManager` is always used to verify installation and retrieve the launcher activity.

## Important Android limits
- Microphone FGS requires explicit microphone permission and must be started while the app is visible on modern Android when using while-in-use microphone permission.
- Notification permission is requested on Android 13+.
- Accessibility is enabled by the user in Android Settings. SANDHYA never silently enables it.
- Overlay is enabled by the user in Android Settings.
- MediaProjection requires the official Android consent dialog for each capture session on modern Android.
- Camera and media permissions are requested only for the features that use them.
- Battery optimization exemption is optional and user-controlled.
- Full arbitrary phone control is not provided; actions are limited to APIs Android exposes.

## Build
1. Open the project in Android Studio.
2. Let Gradle sync.
3. Use JDK 17.
4. Select a physical Android device or emulator.
5. Build > Make Project.
6. Build > Build APK(s).

CLI:
```bash
./gradlew assembleDebug
```

APK:
`app/build/outputs/apk/debug/app-debug.apk`

## First-run setup
1. Open SANDHYA.
2. Grant Microphone.
3. Grant Notifications on Android 13+.
4. If using accessibility commands, open Accessibility and enable SANDHYA.
5. If using floating UI, open Overlay and allow SANDHYA.
6. If using screen capture, press Screen Capture and accept Android's system dialog.
7. Only grant Camera or Photos/Media if those features are used.
8. Battery optimization is optional and device-dependent.

## Voice tests
Say:
- "YouTube kholo"
- "WhatsApp kholo"
- "Instagram kholo"
- "Settings kholo"
- "Camera kholo"
- "Play Store kholo"
- "Maps kholo"

If an app is not installed, SANDHYA does not claim success.

## Overlay
`OverlayService` is a visible, user-authorized `TYPE_APPLICATION_OVERLAY` window. It never attempts hidden overlays. Enable Overlay in Android Settings first.

## Extension points
- `AIService`: structured AI interpretation boundary.
- `CommandParser`: deterministic local parser.
- `AppManager`: package resolution and native launch.
- `AccessibilityService`: supported system/UI actions.
- `VoiceForegroundService`: speech recognition and foreground microphone lifecycle.
- `ScreenCaptureService`: MediaProjection lifecycle.
- `PermissionManager`: actual permission/state checks.
- `TaskManager`: task state model.
