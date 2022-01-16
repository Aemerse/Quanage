package com.aemerse.quanage.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import com.aemerse.quanage.R
import com.aemerse.quanage.constants.QRNGType

class SoundPlayer(context: Context, private val listener: Listener) {
    interface Listener {
        fun onAudioComplete()
        fun onAudioError()
    }

    private val onCompletionListener = OnCompletionListener {
        audioFocusManager.releaseAudioFocus()
        currentlyPlayingMediaPlayer = null
        listener.onAudioComplete()
    }
    private val audioFocusListener: AudioFocusManager.Listener = object : AudioFocusManager.Listener {
        override fun onAudioFocusGranted() {
            if (currentlyPlayingMediaPlayer != null) {
                currentlyPlayingMediaPlayer!!.seekTo(0)
                currentlyPlayingMediaPlayer!!.start()
            }
        }

        override fun onAudioFocusDenied() {
            listener.onAudioError()
        }

        override fun onAudioFocusLost() {
            if (currentlyPlayingMediaPlayer != null) {
                currentlyPlayingMediaPlayer!!.pause()
                currentlyPlayingMediaPlayer = null
            }
        }
    }
    private val rngSoundPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.rng_noise)
    private val diceSoundPlayer: MediaPlayer
    private val coinSoundPlayer: MediaPlayer
    private var currentlyPlayingMediaPlayer: MediaPlayer? = null
    private val audioFocusManager: AudioFocusManager
    fun playSound(@QRNGType rngType: Int) {
        audioFocusManager.releaseAudioFocus()
        if (currentlyPlayingMediaPlayer != null) {
            currentlyPlayingMediaPlayer!!.pause()
            currentlyPlayingMediaPlayer = null
        }
        when (rngType) {
            QRNGType.NUMBER -> currentlyPlayingMediaPlayer = rngSoundPlayer
            QRNGType.DICE -> currentlyPlayingMediaPlayer = diceSoundPlayer
            QRNGType.COINS -> currentlyPlayingMediaPlayer = coinSoundPlayer
        }
        audioFocusManager.requestAudioFocus()
    }

    fun silence() {
        if (currentlyPlayingMediaPlayer != null) {
            audioFocusManager.releaseAudioFocus()
            currentlyPlayingMediaPlayer!!.pause()
            currentlyPlayingMediaPlayer = null
        }
    }

    init {
        rngSoundPlayer.setOnCompletionListener(onCompletionListener)
        diceSoundPlayer = MediaPlayer.create(context, R.raw.dice_roll)
        diceSoundPlayer.setOnCompletionListener(onCompletionListener)
        coinSoundPlayer = MediaPlayer.create(context, R.raw.coin_flip)
        coinSoundPlayer.setOnCompletionListener(onCompletionListener)
        audioFocusManager = AudioFocusManager(context, audioFocusListener)
    }
}