package com.aemerse.quanage.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.quanage.R
import com.aemerse.quanage.databinding.SelectedActivityBinding
import com.aemerse.quanage.utils.loadImageWithTransition

class SelectedActivity : AppCompatActivity() {
    private lateinit var binding: SelectedActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectedActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url:String = intent.extras!!.getString("url")!!
        if (intent.extras!!.containsKey("type") && intent.extras!!.getString("type")=="video") {
            playVideo(url)
        } else {
            displayImage(url)
        }
    }

    private fun playVideo(path: String?) {
        val controller = MediaController(this)
        controller.setAnchorView(binding.videoView)
        controller.setMediaPlayer(binding.videoView)
        binding.videoView.setMediaController(controller)
        binding.videoView.setVideoURI(Uri.parse(path))
        binding.videoView.setOnPreparedListener { mp ->
            binding.videoView.layoutParams.height = (binding.videoView.width.toFloat() * (mp.videoHeight.toFloat() /  mp.videoWidth.toFloat())).toInt()
            binding.videoView.start()
            binding.progressBar.visibility = GONE
        }
        binding.videoView.visibility = View.VISIBLE
        binding.imageView.visibility = GONE
    }

    private fun displayImage(path: String?) {
        binding.videoView.visibility = GONE
        binding.imageView.visibility = View.VISIBLE
        loadImageWithTransition(this, path, binding.imageView,binding.progressBar)
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }
}