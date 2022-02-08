package com.aemerse.quanage.utils

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.aemerse.quanage.R
import com.aemerse.quanage.databinding.ImagesAndVideosActivityBinding

class ImagesAndVideosActivity : AppCompatActivity() {
    private lateinit var binding: ImagesAndVideosActivityBinding
    private lateinit var selections: ArrayList<String>
    private lateinit var selectedCurrently: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImagesAndVideosActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selections = intent.extras!!.get("selections") as ArrayList<String>

        selections.forEachIndexed { index, s ->
            if(index==0){
                selectedCurrently = s
            }
            val iv = LayoutInflater.from(this)
                .inflate(R.layout.scrollitem_image, binding.imageContainer,false)
                    as ImageView

            iv.setOnClickListener {
                selectedCurrently = s
                startSelection()
            }

            iv.setOnLongClickListener { viewIv->
                MaterialAlertDialogBuilder(this,R.style.BottomSheet)
                    .setTitle("Delete Selection?")
                    .setPositiveButton(R.string.proceed) { dialog, which ->
                        removeView(viewIv,selectedCurrently)
                    }
                    .setNeutralButton(R.string.cancel, null)
                true
            }

            Glide.with(this).load(s).into(iv)
            binding.imageContainer.addView(iv)
        }
        startSelection()

        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.send ->{
                    intent.putExtra("selections", selections)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            false
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun startSelection(){
        if(isVideo(selectedCurrently)){
            playVideo(selectedCurrently)
        }
        else{
            displayImage(selectedCurrently)
        }
    }

    private fun removeView(viewIv: View?, s: String) {
        binding.imageContainer.removeView(viewIv)
        selections.remove(s)
    }

    private fun playVideo(path: String?) {
        val controller = MediaController(this)
        controller.setAnchorView(binding.videoView)
        controller.setMediaPlayer(binding.videoView)
        binding.videoView.setMediaController(controller)
        binding.videoView.setVideoPath(path)
        binding.videoView.setOnCompletionListener { binding.videoView.start() }
        binding.videoView.setOnPreparedListener { mp ->
            binding.progressBar.visibility = GONE
            binding.videoView.layoutParams.height = (binding.videoView.width.toFloat() * (mp.videoHeight.toFloat() /  mp.videoWidth.toFloat())).toInt()
            binding.videoView.start()
        }
        binding.videoView.visibility = VISIBLE
        binding.imageView.visibility = GONE
    }

    private fun displayImage(path: String?) {
        binding.videoView.visibility = GONE
        binding.imageView.visibility = VISIBLE
        loadImageWithTransition(this, path, binding.imageView,binding.progressBar)
    }
}