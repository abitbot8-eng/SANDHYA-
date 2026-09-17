package com.sandhya.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(context: Context) {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("hi", "IN")
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sandhya_reply")
    }

    fun close() { tts?.shutdown() }
}
