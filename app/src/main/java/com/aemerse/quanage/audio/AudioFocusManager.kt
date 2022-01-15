package com.aemerse.quanage.audio

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.Handler

class AudioFocusManager internal constructor(context: Context, private val listener: Listener) {
    interface Listener {
        fun onAudioFocusGranted()
        fun onAudioFocusDenied()
        fun onAudioFocusLost()
    }

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Oreo audio focus shenanigans
    private var audioFocusRequest: AudioFocusRequest? = null
    @TargetApi(Build.VERSION_CODES.O)
    private fun initializeOAudioFocusParams() {
        val ttsAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(ttsAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener, Handler())
                .build()
    }

    fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAudioFocusPostO()
        } else {
            requestAudioFocusPreO()
        }
    }

    private fun requestAudioFocusPreO() {
        val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            listener.onAudioFocusGranted()
        } else {
            listener.onAudioFocusDenied()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusPostO() {
        val res = audioManager.requestAudioFocus(audioFocusRequest!!)
        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            listener.onAudioFocusGranted()
        }
    }

    private val audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            releaseAudioFocus()
            listener.onAudioFocusLost()
        }
    }

    fun releaseAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initializeOAudioFocusParams()
        }
    }
}