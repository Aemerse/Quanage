package com.aemerse.quanage.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.quanage.R
import com.aemerse.quanage.databinding.ProfileActivityBinding
import com.aemerse.quanage.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var currentAuth: FirebaseUser
    private lateinit var binding: ProfileActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        binding = ProfileActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.itemIconTintList = null
        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        currentAuth = FirebaseAuth.getInstance().currentUser!!

        binding.emailIdText.text = currentAuth.email
        binding.dobText.text = currentAuth.displayName

        if(currentAuth.phoneNumber.isNullOrEmpty()){
            binding.phonell.visibility = GONE
        }
        else{
            binding.phoneNumberText.text = currentAuth.phoneNumber
        }

        Glide.with(this).load(currentAuth.photoUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(60)))
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                    binding.progressBar.visibility = GONE
                    binding.faceRecognitionPic.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    binding.progressBar.visibility = GONE
                    binding.faceRecognitionPic.visibility = View.VISIBLE
                    return false
                }
            }).into(binding.faceRecognitionPic)

        binding.faceRecognitionPic.setOnClickListener {
            intent = Intent(applicationContext, SelectedImage::class.java)
            intent.putExtra("file", currentAuth.photoUrl)
            startActivity(intent)
        }

        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.signOut -> {
                    GifDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setPositiveBtnText("Ok")
                        .setPositiveBtnBackground("#22b573")
                        .setNegativeBtnText("No")
                        .setNegativeBtnBackground("#c1272d")
                        .setGifResource(R.raw.cycling)
                        .isCancellable(true)
                        .OnPositiveClicked(object : GifDialog.GifDialogListener {
                            override fun onClick() {
                                AuthUI.getInstance()
                                    .signOut(this@ProfileActivity)
                                    .addOnCompleteListener { // user is now signed out
                                        startActivity(Intent(this@ProfileActivity, SplashActivity::class.java))
                                        finish()
                                    }
                            }
                        })
                        .OnNegativeClicked(object : GifDialog.GifDialogListener { override fun onClick() {} })
                        .build()
                }
                R.id.update -> {
                    if (!askForPermissions(this, permissionsToGive, permissionsRequest)) {
                        val photoPickerIntent = Intent(Intent.ACTION_PICK)
                        photoPickerIntent.type = "image/*"
                        startActivityForResult(photoPickerIntent, 1)
                    }
                }
            }
            false
        }
        binding.content.startRippleAnimation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!askForPermissions(this, permissionsToGive, permissionsRequest)) {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, 1)
        }
    }


    private fun uploadFile(file: String) {
        GlobalScope.launch(Dispatchers.Default) {
            FirebaseStorage.getInstance().reference.child("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("ProfilePic")
                .putFile(Uri.fromFile(Compressor.compress(applicationContext, File(file))))
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri: Uri ->
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build()

                            FirebaseAuth.getInstance().currentUser!!.updateProfile(profileUpdates).addOnSuccessListener {
                                recreate()
                            }
                        }
                }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                1 -> {
                    uploadFile(getRealPathFromURI(data.data!!, this)!!)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.home -> {
                intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.chat -> {
                intent = Intent(applicationContext, ChatsDM::class.java)
                startActivity(intent)
            }
        }
        false
    }
}