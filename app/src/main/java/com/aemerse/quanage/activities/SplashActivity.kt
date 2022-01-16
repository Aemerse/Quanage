package com.aemerse.quanage.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.quanage.R
import com.firebase.ui.auth.AuthUI
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private val signInCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        FirebaseApp.initializeApp(applicationContext)

        Handler(Looper.getMainLooper()).postDelayed({
            loginHere()
        }, 3000)
    }

    private fun loginHere(){
        if (FirebaseAuth.getInstance().currentUser!=null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
        else{
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                            .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                            .setTheme(R.style.AuthTheme)
                            .setLogo(R.drawable.logo)
                            .build(), signInCode
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loginHere()
    }

    override fun onBackPressed() {
        // Do nothing
    }
}