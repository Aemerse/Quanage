package com.aemerse.quanage.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.quanage.R
import com.aemerse.quanage.databinding.SelectedImageActivityBinding
import com.bumptech.glide.Glide

class SelectedImage : AppCompatActivity() {
    private lateinit var binding: SelectedImageActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectedImageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this).load(intent.extras!!.get("file")).into(binding.imagePreview)

        binding.titleIcon.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }
}