package com.aemerse.quanage.init

import android.app.Application
import com.google.firebase.FirebaseApp
import com.joanzapata.iconify.Iconify
import com.joanzapata.iconify.fonts.FontAwesomeModule
import com.joanzapata.iconify.fonts.IoniconsModule
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Iconify.with(IoniconsModule()).with(FontAwesomeModule())
        EmojiManager.install(GoogleEmojiProvider())
        FirebaseApp.initializeApp(applicationContext)
    }
}