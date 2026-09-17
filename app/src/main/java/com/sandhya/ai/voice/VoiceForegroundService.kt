package com.sandhya.ai.voice
import com.sandhya.ai.screen.ScreenCaptureService

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.os.*
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sandhya.ai.R
import com.sandhya.ai.app.AppManager
import com.sandhya.ai.command.CommandParser
import com.sandhya.ai.command.CommandType
import com.sandhya.ai.core.AIService

class VoiceForegroundService : Service() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var voice: VoiceManager
    private lateinit var appManager: AppManager
    private val ai = AIService()

    override fun onCreate() {
        super.onCreate()
        voice = VoiceManager(this)
        appManager = AppManager(this)
        createChannel()
        val notification = NotificationCompat.Builder(this, "sandhya_voice")
            .setContentTitle("SANDHYA is listening")
            .setContentText("Microphone active according to your selected listening mode.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                100,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(100, notification)
        }
        startRecognition()
    }

    private fun startRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            voice.speak("Boss, speech recognition available nahi hai.")
            stopSelf()
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).also { sr ->
            sr.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    // Android's SpeechRecognizer ends an utterance; in continuous mode
                    // the service resumes the session without recreating the foreground service.
                }
                override fun onError(error: Int) {
                    Handler(Looper.getMainLooper()).postDelayed({ startRecognition() }, 350)
                }
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrBlank()) execute(text)
                    Handler(Looper.getMainLooper()).postDelayed({ startRecognition() }, 250)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            sr.startListening(intent)
        }
    }

    private fun execute(text: String) {
        val command = ai.interpret(text)
        when (command.type) {
            CommandType.APP_OPEN -> {
                voice.speak("Bilkul Boss, ${command.target} khol rahi hoon.")
                val result = appManager.launch(command.target!!)
                if (result.isFailure) voice.speak("Boss, ye app phone me installed nahi hai.")
            }
            CommandType.ACCESSIBILITY_ACTION -> {
                voice.speak("Boss, Accessibility action ke liye permission chahiye.")
            }
            else -> voice.speak("Boss, ye command abhi supported nahi hai.")
        }
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("sandhya_voice", "SANDHYA Voice", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onDestroy() {
        recognizer?.destroy()
        voice.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= 26)
                context.startForegroundService(Intent(context, VoiceForegroundService::class.java))
            else context.startService(Intent(context, VoiceForegroundService::class.java))
        }

        fun startScreenCapture(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent)
            else context.startService(intent)
        }
    }
}
