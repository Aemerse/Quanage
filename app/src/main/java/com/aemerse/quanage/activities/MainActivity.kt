package com.aemerse.quanage.activities

import android.content.Intent
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.aemerse.quanage.R
import com.aemerse.quanage.adapters.HomepageTabsAdapter
import com.aemerse.quanage.audio.SoundPlayer
import com.aemerse.quanage.constants.QRNGType
import com.aemerse.quanage.databinding.HomepageBinding
import com.aemerse.quanage.dialogs.HistoryDialog
import com.aemerse.quanage.persistence.HistoryDataManager
import com.aemerse.quanage.persistence.PreferencesManager
import com.aemerse.quanage.utils.ShakeManager
import com.aemerse.quanage.utils.ShakeManager.Companion.get
import com.aemerse.quanage.utils.hideKeyboard
import com.aemerse.quanage.utils.showLongToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.seismic.ShakeDetector

class MainActivity : AppCompatActivity(), ShakeDetector.Listener {
    private var soundPlayer: SoundPlayer? = null
    private var shakeDetector: ShakeDetector? = null
    private var disableGeneration = false
    private var shakeManager: ShakeManager? = null
    private var preferencesManager: PreferencesManager? = null
    private var rngHistoryDialog: HistoryDialog? = null
    private var diceHistoryDialog: HistoryDialog? = null
    private var coinsHistoryDialog: HistoryDialog? = null

    private lateinit var binding: HomepageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        binding = HomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        volumeControlStream = AudioManager.STREAM_MUSIC

        binding.bottomnavview.itemIconTintList = null
        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val soundListener: SoundPlayer.Listener = object : SoundPlayer.Listener {
            override fun onAudioComplete() {
                disableGeneration = false
            }

            override fun onAudioError() {
                showLongToast(R.string.sound_fail, this@MainActivity)
            }
        }
        soundPlayer = SoundPlayer(this, soundListener)
        preferencesManager = PreferencesManager(this)
        val tabsAdapter = HomepageTabsAdapter(supportFragmentManager, resources.getStringArray(R.array.homepageTabStrings))
        binding.homePager.adapter = tabsAdapter
        binding.homePager.offscreenPageLimit = 3
        binding.homeTabs.setupWithViewPager(binding.homePager)
        shakeManager = get()
        shakeDetector = ShakeDetector(this)

        rngHistoryDialog = HistoryDialog(this, QRNGType.NUMBER, true)
        diceHistoryDialog = HistoryDialog(this, QRNGType.DICE, true)
        coinsHistoryDialog = HistoryDialog(this, QRNGType.COINS, true)
        HistoryDataManager[this]!!.initialHistory

        binding.homePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                hideKeyboard(this@MainActivity)
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}

        })
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.history -> {
                    showProperHistoryDialog()
                }
            }
            false
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.profile -> {
                intent = Intent(applicationContext, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        false
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (preferencesManager!!.isShakeEnabled) {
            shakeDetector!!.stop()
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        if (preferencesManager!!.isShakeEnabled) {
            shakeDetector!!.start(getSystemService(SENSOR_SERVICE) as SensorManager)
        }
    }

    fun showSnackbar(message: String?) {
        com.aemerse.quanage.utils.showSnackbar(binding.root, message, this)
    }


    fun playSound(@QRNGType rngType: Int) {
        if (!preferencesManager!!.shouldPlaySounds()) {
            return
        }
        soundPlayer!!.playSound(rngType)
    }

    override fun hearShake() {
        if (preferencesManager!!.shouldPlaySounds()) {
            if (!disableGeneration) {
                disableGeneration = true
                shakeManager!!.onShakeDetected(binding.homePager.currentItem)
            }
        } else {
            shakeManager!!.onShakeDetected(binding.homePager.currentItem)
        }
    }

    private fun showProperHistoryDialog() {
        when (binding.homePager.currentItem) {
            0 -> rngHistoryDialog!!.show()
            1 -> diceHistoryDialog!!.show()
            2 -> coinsHistoryDialog!!.show()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (preferencesManager!!.isShakeEnabled) {
            shakeDetector!!.stop()
        }
        soundPlayer!!.silence()
    }
}